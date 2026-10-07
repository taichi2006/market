package com.mycompany.quanlysieuthi.report.dao;

import com.mycompany.quanlysieuthi.config.JpaUtil;
import com.mycompany.quanlysieuthi.purchasereceipt.entity.Product;
import com.mycompany.quanlysieuthi.report.entity.InventoryReport;
import com.mycompany.quanlysieuthi.report.entity.InventoryReportDetail;
import com.mycompany.quanlysieuthi.report.dto.response.common.StockAuditEmployeeDto;
import com.mycompany.quanlysieuthi.report.dto.response.detail.StockAuditDetailResponse;
import com.mycompany.quanlysieuthi.report.dto.response.detail.StockDiscrepancyDetailItemDto;
import com.mycompany.quanlysieuthi.report.dto.response.list.StockAuditListItemDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Report transactions.
 */
public class ReportDao {

    /**
     * Tìm thông tin sản phẩm theo productId.
     */
    public Optional<Product> findProductById(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Product product = em.find(Product.class, productId.trim());
            return Optional.ofNullable(product);
        } finally {
            em.close();
        }
    }

    /**
     * Lưu giao dịch tạo biên bản kiểm kê và danh sách chi tiết các mặt hàng vào
     * Neon DB.
     */
    public void saveAuditReportTransaction(InventoryReport report, List<InventoryReportDetail> details) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            em.persist(report);

            if (details != null) {
                for (InventoryReportDetail detail : details) {
                    em.persist(detail);
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            throw new RuntimeException("Lỗi giao dịch khi lưu biên bản kiểm kê kho: " + root.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Lấy danh sách các biên bản kiểm kê kho kèm thông tin nhân viên và số lượng
     * mặt hàng bị lệch.
     */
    public List<StockAuditListItemDto> findAuditReports(int page, int limit,
            String employeeId,
            LocalDateTime fromDateTime,
            LocalDateTime toDateTime) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT ir.reportId, ir.reportDate, e.employeeId, e.fullName, e.username, " +
                            "SUM(CASE WHEN d.actualQuantity <> d.systemQuantity THEN 1L ELSE 0L END) " +
                            "FROM InventoryReport ir " +
                            "LEFT JOIN Employee e ON ir.employeeId = e.employeeId " +
                            "LEFT JOIN InventoryReportDetail d ON ir.reportId = d.reportId " +
                            "WHERE 1=1 ");

            if (employeeId != null && !employeeId.trim().isEmpty()) {
                jpql.append("AND ir.employeeId = :employeeId ");
            }
            if (fromDateTime != null) {
                jpql.append("AND ir.reportDate >= :fromDateTime ");
            }
            if (toDateTime != null) {
                jpql.append("AND ir.reportDate <= :toDateTime ");
            }

            jpql.append("GROUP BY ir.reportId, ir.reportDate, e.employeeId, e.fullName, e.username ");
            jpql.append("ORDER BY ir.reportDate DESC");

            TypedQuery<Object[]> query = em.createQuery(jpql.toString(), Object[].class);

            if (employeeId != null && !employeeId.trim().isEmpty()) {
                query.setParameter("employeeId", employeeId.trim());
            }
            if (fromDateTime != null) {
                query.setParameter("fromDateTime", fromDateTime);
            }
            if (toDateTime != null) {
                query.setParameter("toDateTime", toDateTime);
            }

            if (limit > 0) {
                int offset = (page - 1) * limit;
                query.setFirstResult(offset);
                query.setMaxResults(limit);
            }

            List<Object[]> rawList = query.getResultList();
            List<StockAuditListItemDto> result = new ArrayList<>();
            DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            DateTimeFormatter titleFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (Object[] row : rawList) {
                String reportId = (String) row[0];
                LocalDateTime reportDate = (LocalDateTime) row[1];
                String empId = (String) row[2];
                String empFullName = (String) row[3];
                String empUsername = (String) row[4];
                int totalDiscrepancyItems = (row[5] instanceof Number) ? ((Number) row[5]).intValue() : 0;

                String formattedDate = reportDate != null
                        ? reportDate.atOffset(ZoneOffset.UTC).format(isoFormatter)
                        : null;

                String title = "Kiểm kê kho định kỳ " + (reportDate != null ? reportDate.format(titleFormatter) : "");

                StockAuditEmployeeDto empDto = null;
                if (empId != null) {
                    empDto = new StockAuditEmployeeDto(empId, empFullName, empUsername);
                }

                result.add(new StockAuditListItemDto(
                        reportId,
                        title,
                        formattedDate,
                        totalDiscrepancyItems,
                        empDto));
            }

            return result;
        } finally {
            em.close();
        }
    }

    /**
     * Lấy thông tin chi tiết một biên bản kiểm kê kho theo reportId.
     */
    public Optional<StockAuditDetailResponse> findAuditReportDetailById(String reportId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String reportJpql = "SELECT ir.reportId, ir.reportDate, e.employeeId, e.fullName, e.username " +
                    "FROM InventoryReport ir " +
                    "LEFT JOIN Employee e ON ir.employeeId = e.employeeId " +
                    "WHERE ir.reportId = :reportId";

            List<Object[]> reportRows = em.createQuery(reportJpql, Object[].class)
                    .setParameter("reportId", reportId)
                    .getResultList();

            if (reportRows.isEmpty()) {
                return Optional.empty();
            }

            Object[] reportRow = reportRows.get(0);
            LocalDateTime reportDate = (LocalDateTime) reportRow[1];
            String empId = (String) reportRow[2];
            String empFullName = (String) reportRow[3];
            String empUsername = (String) reportRow[4];

            StockAuditEmployeeDto empDto = null;
            if (empId != null) {
                empDto = new StockAuditEmployeeDto(empId, empFullName, empUsername);
            }

            DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            DateTimeFormatter titleFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            String createdAt = reportDate != null
                    ? reportDate.atOffset(ZoneOffset.UTC).format(isoFormatter)
                    : null;
            String auditTitle = "Kiểm kê kho định kỳ " + (reportDate != null ? reportDate.format(titleFormatter) : "");
            String note = "Biên bản kiểm kê kho định kỳ";

            String detailJpql = "SELECT d.reportId, d.productId, p.productName, c.categoryName, " +
                    "d.systemQuantity, d.actualQuantity " +
                    "FROM InventoryReportDetail d " +
                    "LEFT JOIN Product p ON d.productId = p.productId " +
                    "LEFT JOIN ProductCategory c ON p.categoryId = c.categoryId " +
                    "WHERE d.reportId = :reportId";

            List<Object[]> detailRows = em.createQuery(detailJpql, Object[].class)
                    .setParameter("reportId", reportId)
                    .getResultList();

            List<StockDiscrepancyDetailItemDto> items = new ArrayList<>();
            int totalDiscrepancyItems = 0;

            for (Object[] dRow : detailRows) {
                String rId = (String) dRow[0];
                String pId = (String) dRow[1];
                String pName = (String) dRow[2];
                String cName = (String) dRow[3];
                Integer sysQty = (Integer) dRow[4];
                Integer actQty = (Integer) dRow[5];

                int systemQty = sysQty != null ? sysQty : 0;
                int actualQty = actQty != null ? actQty : 0;
                int diff = actualQty - systemQty;

                String status;
                String reason;
                if (diff < 0) {
                    status = "SHORTAGE";
                    reason = "Thiếu " + Math.abs(diff) + " sản phẩm so với tồn kho hệ thống";
                    totalDiscrepancyItems++;
                } else if (diff > 0) {
                    status = "SURPLUS";
                    reason = "Thừa " + diff + " sản phẩm so với tồn kho hệ thống";
                    totalDiscrepancyItems++;
                } else {
                    status = "MATCH";
                    reason = "Khớp số lượng tồn kho";
                }

                String detailId = rId + "_" + pId;

                items.add(new StockDiscrepancyDetailItemDto(
                        detailId,
                        pId,
                        pName != null ? pName : "",
                        cName != null ? cName : "",
                        systemQty,
                        actualQty,
                        diff,
                        status,
                        reason));
            }

            return Optional.of(new StockAuditDetailResponse(
                    reportId,
                    auditTitle,
                    note,
                    createdAt,
                    empDto,
                    totalDiscrepancyItems,
                    items));
        } finally {
            em.close();
        }
    }
}
