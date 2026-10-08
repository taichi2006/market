package com.mycompany.quanlysieuthi.modules.payment;

import com.mycompany.quanlysieuthi.config.JpaUtil;
import com.mycompany.quanlysieuthi.modules.customer.Invoice;
import com.mycompany.quanlysieuthi.modules.payment.dto.PaymentSummaryDto;
import com.mycompany.quanlysieuthi.modules.payment.dto.PaymentTransactionDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Lớp truy xuất cơ sở dữ liệu (DAO) cho các nghiệp vụ thanh toán sử dụng JPA EntityManager.
 */
public class PaymentDao {

    /**
     * Tìm hóa đơn theo mã hóa đơn (invoice_id).
     */
    public Optional<Invoice> findInvoiceById(String invoiceId) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Invoice invoice = em.find(Invoice.class, invoiceId.trim());
            return Optional.ofNullable(invoice);
        } finally {
            em.close();
        }
    }

    /**
     * Tìm giao dịch thanh toán theo mã giao dịch (transaction_id).
     */
    public Optional<Payment> findPaymentByTransactionId(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Payment payment = em.find(Payment.class, transactionId.trim());
            return Optional.ofNullable(payment);
        } finally {
            em.close();
        }
    }

    /**
     * Tìm giao dịch thanh toán mới nhất gắn với một hóa đơn.
     */
    public Optional<Payment> findPaymentByInvoiceId(String invoiceId) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Payment> list = em.createQuery(
                    "SELECT p FROM Payment p WHERE p.invoiceId = :invoiceId ORDER BY p.paymentTime DESC",
                    Payment.class
            ).setParameter("invoiceId", invoiceId.trim()).getResultList();
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Lưu giao dịch tiền mặt và cập nhật trạng thái hóa đơn trong cùng một Transaction nguyên tử.
     */
    public void saveCashPaymentTransaction(Payment payment, Invoice invoice) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(payment);
            em.merge(invoice);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi khi lưu giao dịch thanh toán tiền mặt: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Lưu thông tin một bản ghi thanh toán vào bảng PAYMENT.
     */
    public Payment savePayment(Payment payment) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Payment result = em.merge(payment);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi khi lưu thông tin thanh toán: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Cập nhật trạng thái thanh toán và trạng thái hóa đơn (khi nhận callback từ VNPay IPN / Return).
     */
    public void updatePaymentAndInvoiceStatus(String transactionId, String paymentStatus, String invoiceStatus) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Payment payment = em.find(Payment.class, transactionId);
            if (payment != null) {
                payment.setStatus(paymentStatus);
                payment.setPaymentTime(LocalDateTime.now());
                em.merge(payment);

                if (payment.getInvoiceId() != null) {
                    Invoice invoice = em.find(Invoice.class, payment.getInvoiceId());
                    if (invoice != null) {
                        invoice.setStatus(invoiceStatus);
                        em.merge(invoice);
                    }
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi khi cập nhật trạng thái giao dịch: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Lấy danh sách giao dịch thanh toán theo bộ lọc và phân trang (kết hợp JOIN với hóa đơn và nhân viên thu ngân).
     */
    @SuppressWarnings("unchecked")
    public List<PaymentTransactionDto> findTransactions(int page, int limit,
                                                        String paymentMethod,
                                                        String status,
                                                        LocalDateTime fromDateTime,
                                                        LocalDateTime toDateTime,
                                                        String cashierEmployeeId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT p.transaction_id, p.invoice_id, p.payment_method, p.amount, p.status, p.payment_time, e.full_name " +
                    "FROM payment p " +
                    "JOIN invoice i ON p.invoice_id = i.invoice_id " +
                    "LEFT JOIN employee e ON i.employee_id = e.employee_id " +
                    "WHERE 1=1 "
            );
            Map<String, Object> params = new HashMap<>();

            // Nếu là thu ngân thì chỉ xem các hóa đơn do chính mình lập
            if (cashierEmployeeId != null && !cashierEmployeeId.trim().isEmpty()) {
                sql.append(" AND i.employee_id = :cashierEmployeeId ");
                params.put("cashierEmployeeId", cashierEmployeeId.trim());
            }

            // Lọc theo phương thức thanh toán
            if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
                sql.append(" AND UPPER(p.payment_method) = :paymentMethod ");
                params.put("paymentMethod", paymentMethod.trim().toUpperCase());
            }

            // Lọc theo trạng thái giao dịch
            if (status != null && !status.trim().isEmpty()) {
                sql.append(" AND UPPER(p.status) = :status ");
                params.put("status", status.trim().toUpperCase());
            }

            // Lọc theo khoảng thời gian thanh toán
            if (fromDateTime != null) {
                sql.append(" AND p.payment_time >= :fromDateTime ");
                params.put("fromDateTime", fromDateTime);
            }

            if (toDateTime != null) {
                sql.append(" AND p.payment_time <= :toDateTime ");
                params.put("toDateTime", toDateTime);
            }

            sql.append(" ORDER BY p.payment_time DESC, p.transaction_id DESC ");

            Query query = em.createNativeQuery(sql.toString());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }

            int offset = Math.max(0, (page - 1) * limit);
            query.setFirstResult(offset);
            query.setMaxResults(limit);

            List<Object[]> rows = query.getResultList();
            List<PaymentTransactionDto> result = new ArrayList<>();

            for (Object[] row : rows) {
                String txId = (row[0] != null) ? row[0].toString() : "";
                String invId = (row[1] != null) ? row[1].toString() : "";
                String method = (row[2] != null) ? row[2].toString() : "";
                BigDecimal amount = (row[3] != null) ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
                String st = (row[4] != null) ? row[4].toString() : "";

                String timeStr = "";
                if (row[5] instanceof Timestamp) {
                    timeStr = ((Timestamp) row[5]).toInstant().toString();
                } else if (row[5] instanceof LocalDateTime) {
                    timeStr = ((LocalDateTime) row[5]).atZone(ZoneId.systemDefault()).toInstant().toString();
                } else if (row[5] != null) {
                    timeStr = row[5].toString();
                }

                String cashierName = (row[6] != null) ? row[6].toString() : "";

                result.add(new PaymentTransactionDto(txId, invId, method, amount, st, timeStr, cashierName));
            }

            return result;
        } finally {
            em.close();
        }
    }

    /**
     * Tính toán tổng doanh thu theo tiền mặt, online và tổng số tiền (phục vụ đối soát kết ca).
     */
    public PaymentSummaryDto getSummary(LocalDateTime fromDateTime,
                                        LocalDateTime toDateTime,
                                        String cashierEmployeeId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT " +
                    "COALESCE(SUM(CASE WHEN p.status = 'PAID' AND UPPER(p.payment_method) = 'CASH' THEN p.amount ELSE 0 END), 0) AS total_cash, " +
                    "COALESCE(SUM(CASE WHEN p.status = 'PAID' AND UPPER(p.payment_method) IN ('QR', 'BANK_TRANSFER') THEN p.amount ELSE 0 END), 0) AS total_online, " +
                    "COALESCE(SUM(CASE WHEN p.status = 'PAID' THEN p.amount ELSE 0 END), 0) AS total_revenue " +
                    "FROM payment p " +
                    "JOIN invoice i ON p.invoice_id = i.invoice_id " +
                    "WHERE 1=1 "
            );
            Map<String, Object> params = new HashMap<>();

            if (cashierEmployeeId != null && !cashierEmployeeId.trim().isEmpty()) {
                sql.append(" AND i.employee_id = :cashierEmployeeId ");
                params.put("cashierEmployeeId", cashierEmployeeId.trim());
            }

            if (fromDateTime != null) {
                sql.append(" AND p.payment_time >= :fromDateTime ");
                params.put("fromDateTime", fromDateTime);
            }

            if (toDateTime != null) {
                sql.append(" AND p.payment_time <= :toDateTime ");
                params.put("toDateTime", toDateTime);
            }

            Query query = em.createNativeQuery(sql.toString());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }

            Object singleResult = query.getSingleResult();
            if (singleResult instanceof Object[]) {
                Object[] row = (Object[]) singleResult;
                BigDecimal totalCash = (row[0] != null) ? new BigDecimal(row[0].toString()) : BigDecimal.ZERO;
                BigDecimal totalOnline = (row[1] != null) ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
                BigDecimal totalRevenue = (row[2] != null) ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
                return new PaymentSummaryDto(totalCash, totalOnline, totalRevenue);
            }

            return new PaymentSummaryDto();
        } finally {
            em.close();
        }
    }
}
