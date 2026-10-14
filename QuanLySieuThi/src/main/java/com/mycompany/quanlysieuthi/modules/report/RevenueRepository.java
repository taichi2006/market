package com.mycompany.quanlysieuthi.modules.report;

import com.mycompany.quanlysieuthi.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Repository handling database aggregation queries for Revenue and Excel exports.
 */
public class RevenueRepository {

    public ReportResponse.RevenueSummaryDto getRevenueSummary(LocalDateTime from, LocalDateTime to) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // 1. Invoice summary
            String invJpql = "SELECT COUNT(i), COALESCE(SUM(i.originalTotal), 0), " +
                             "COALESCE(SUM(COALESCE(i.discountAmount, 0)), 0), " +
                             "COALESCE(SUM(i.originalTotal - COALESCE(i.discountAmount, 0)), 0) " +
                             "FROM Invoice i WHERE i.status = 'PAID' AND i.createdAt >= :from AND i.createdAt <= :to";
            Query invQ = em.createQuery(invJpql);
            invQ.setParameter("from", from);
            invQ.setParameter("to", to);
            Object[] invRes = (Object[]) invQ.getSingleResult();

            long totalInvoices = invRes[0] != null ? ((Number) invRes[0]).longValue() : 0L;
            BigDecimal originalTotal = invRes[1] != null ? (BigDecimal) invRes[1] : BigDecimal.ZERO;
            BigDecimal totalDiscount = invRes[2] != null ? (BigDecimal) invRes[2] : BigDecimal.ZERO;
            BigDecimal totalRevenue = invRes[3] != null ? (BigDecimal) invRes[3] : BigDecimal.ZERO;

            // 2. Payment summary (CASH vs ONLINE)
            String payJpql = "SELECT " +
                             "COALESCE(SUM(CASE WHEN UPPER(p.paymentMethod) = 'CASH' THEN p.amount ELSE 0 END), 0), " +
                             "COALESCE(SUM(CASE WHEN UPPER(p.paymentMethod) IN ('QR', 'BANK_TRANSFER') THEN p.amount ELSE 0 END), 0) " +
                             "FROM Payment p WHERE p.status = 'PAID' AND p.paymentTime >= :from AND p.paymentTime <= :to";
            Query payQ = em.createQuery(payJpql);
            payQ.setParameter("from", from);
            payQ.setParameter("to", to);
            Object[] payRes = (Object[]) payQ.getSingleResult();

            BigDecimal cash = payRes[0] != null ? (BigDecimal) payRes[0] : BigDecimal.ZERO;
            BigDecimal online = payRes[1] != null ? (BigDecimal) payRes[1] : BigDecimal.ZERO;
            ReportResponse.PaymentMethodBreakdownDto breakdown = new ReportResponse.PaymentMethodBreakdownDto(cash, online);

            // 3. Return receipt (refund)
            String refJpql = "SELECT COALESCE(SUM(r.refundAmount), 0) FROM ReturnReceipt r " +
                             "WHERE r.createdAt >= :from AND r.createdAt <= :to";
            Query refQ = em.createQuery(refJpql);
            refQ.setParameter("from", from);
            refQ.setParameter("to", to);
            BigDecimal totalRefund = (BigDecimal) refQ.getSingleResult();
            if (totalRefund == null) totalRefund = BigDecimal.ZERO;

            BigDecimal netRevenue = totalRevenue.subtract(totalRefund);

            return new ReportResponse.RevenueSummaryDto(
                    totalRevenue,
                    originalTotal,
                    totalDiscount,
                    totalRefund,
                    netRevenue,
                    totalInvoices,
                    breakdown
            );
        } finally {
            em.close();
        }
    }

    public List<ReportResponse.RevenueTimelineItemDto> getRevenueTimeline(LocalDateTime from, LocalDateTime to, String type) {
        String pattern;
        if ("day".equalsIgnoreCase(type)) {
            pattern = "YYYY-MM-DD HH24:00";
        } else if ("year".equalsIgnoreCase(type)) {
            pattern = "YYYY-MM";
        } else {
            pattern = "YYYY-MM-DD";
        }

        EntityManager em = JpaUtil.getEntityManager();
        try {
            Map<String, Object[]> timelineMap = new TreeMap<>();

            String invSql = "SELECT TO_CHAR(i.created_at, :pattern) AS tp, " +
                            "COUNT(i.invoice_id) AS inv_count, " +
                            "COALESCE(SUM(i.original_total - COALESCE(i.discount_amount, 0)), 0) AS rev " +
                            "FROM invoice i WHERE i.status = 'PAID' " +
                            "AND i.created_at >= :from AND i.created_at <= :to " +
                            "GROUP BY TO_CHAR(i.created_at, :pattern) ORDER BY tp ASC";
            Query invQ = em.createNativeQuery(invSql);
            invQ.setParameter("pattern", pattern);
            invQ.setParameter("from", from);
            invQ.setParameter("to", to);
            List<?> invList = invQ.getResultList();
            for (Object obj : invList) {
                Object[] row = (Object[]) obj;
                String tp = (String) row[0];
                long cnt = ((Number) row[1]).longValue();
                BigDecimal rev = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
                timelineMap.put(tp, new Object[]{cnt, rev, BigDecimal.ZERO, BigDecimal.ZERO});
            }

            String paySql = "SELECT TO_CHAR(p.payment_time, :pattern) AS tp, " +
                            "COALESCE(SUM(CASE WHEN UPPER(p.payment_method) = 'CASH' THEN p.amount ELSE 0 END), 0) AS cash, " +
                            "COALESCE(SUM(CASE WHEN UPPER(p.payment_method) IN ('QR', 'BANK_TRANSFER') THEN p.amount ELSE 0 END), 0) AS online " +
                            "FROM payment p WHERE p.status = 'PAID' " +
                            "AND p.payment_time >= :from AND p.payment_time <= :to " +
                            "GROUP BY TO_CHAR(p.payment_time, :pattern) ORDER BY tp ASC";
            Query payQ = em.createNativeQuery(paySql);
            payQ.setParameter("pattern", pattern);
            payQ.setParameter("from", from);
            payQ.setParameter("to", to);
            List<?> payList = payQ.getResultList();
            for (Object obj : payList) {
                Object[] row = (Object[]) obj;
                String tp = (String) row[0];
                BigDecimal cash = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
                BigDecimal online = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
                if (!timelineMap.containsKey(tp)) {
                    timelineMap.put(tp, new Object[]{0L, BigDecimal.ZERO, cash, online});
                } else {
                    Object[] entry = timelineMap.get(tp);
                    entry[2] = cash;
                    entry[3] = online;
                }
            }

            List<ReportResponse.RevenueTimelineItemDto> result = new ArrayList<>();
            for (Map.Entry<String, Object[]> entry : timelineMap.entrySet()) {
                String tp = entry.getKey();
                Object[] data = entry.getValue();
                long count = (Long) data[0];
                BigDecimal rev = (BigDecimal) data[1];
                BigDecimal cash = (BigDecimal) data[2];
                BigDecimal online = (BigDecimal) data[3];
                result.add(new ReportResponse.RevenueTimelineItemDto(tp, rev, count, cash, online));
            }
            return result;
        } finally {
            em.close();
        }
    }

    public List<Object[]> findPurchaseReceiptRowsForExcel(String receiptId, LocalDateTime from, LocalDateTime to) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT v.voucherId, v.createdAt, pr.supplierName, e.fullName, " +
                    "p.productId, p.productName, pc.categoryName, prd.expirationDate, " +
                    "prd.purchasedQuantity, prd.purchasePrice, (prd.purchasedQuantity * prd.purchasePrice) " +
                    "FROM Voucher v " +
                    "JOIN PurchaseReceipt pr ON v.voucherId = pr.voucherId " +
                    "JOIN PurchaseReceiptDetail prd ON v.voucherId = prd.voucherId " +
                    "LEFT JOIN Employee e ON v.employeeId = e.employeeId " +
                    "LEFT JOIN Product p ON prd.productId = p.productId " +
                    "LEFT JOIN ProductCategory pc ON p.categoryId = pc.categoryId " +
                    "WHERE 1=1 ");

            if (receiptId != null && !receiptId.trim().isEmpty()) {
                jpql.append("AND v.voucherId = :receiptId ");
            }
            if (from != null) {
                jpql.append("AND v.createdAt >= :from ");
            }
            if (to != null) {
                jpql.append("AND v.createdAt <= :to ");
            }
            jpql.append("ORDER BY v.createdAt DESC, v.voucherId ASC");

            TypedQuery<Object[]> q = em.createQuery(jpql.toString(), Object[].class);
            if (receiptId != null && !receiptId.trim().isEmpty()) {
                q.setParameter("receiptId", receiptId.trim());
            }
            if (from != null) {
                q.setParameter("from", from);
            }
            if (to != null) {
                q.setParameter("to", to);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Object[]> findStockDiscrepancyRowsForExcel(String receiptId, LocalDateTime from, LocalDateTime to) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT ir.reportId, ir.reportDate, e.fullName, " +
                    "p.productId, p.productName, pc.categoryName, " +
                    "ird.systemQuantity, ird.actualQuantity, (ird.actualQuantity - ird.systemQuantity), " +
                    "COALESCE(p.unitPrice, 0) " +
                    "FROM InventoryReport ir " +
                    "JOIN InventoryReportDetail ird ON ir.reportId = ird.reportId " +
                    "LEFT JOIN Employee e ON ir.employeeId = e.employeeId " +
                    "LEFT JOIN Product p ON ird.productId = p.productId " +
                    "LEFT JOIN ProductCategory pc ON p.categoryId = pc.categoryId " +
                    "WHERE 1=1 ");

            if (receiptId != null && !receiptId.trim().isEmpty()) {
                jpql.append("AND ir.reportId = :receiptId ");
            }
            if (from != null) {
                jpql.append("AND ir.reportDate >= :from ");
            }
            if (to != null) {
                jpql.append("AND ir.reportDate <= :to ");
            }
            jpql.append("ORDER BY ir.reportDate DESC, ir.reportId ASC");

            TypedQuery<Object[]> q = em.createQuery(jpql.toString(), Object[].class);
            if (receiptId != null && !receiptId.trim().isEmpty()) {
                q.setParameter("receiptId", receiptId.trim());
            }
            if (from != null) {
                q.setParameter("from", from);
            }
            if (to != null) {
                q.setParameter("to", to);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
