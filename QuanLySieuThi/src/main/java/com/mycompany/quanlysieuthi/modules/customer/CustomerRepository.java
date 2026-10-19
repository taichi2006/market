package com.mycompany.quanlysieuthi.modules.customer;

import com.mycompany.quanlysieuthi.common.base.BaseRepository;
import com.mycompany.quanlysieuthi.common.exception.AppException;
import com.mycompany.quanlysieuthi.entity.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository handling database operations for Customer entity.
 */
public class CustomerRepository extends BaseRepository<Customer, String> {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public CustomerRepository() {
        super(Customer.class);
    }

    public Optional<Customer> findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = getEntityManager();
        try {
            List<Customer> list = em.createQuery(
                    "SELECT c FROM Customer c WHERE c.phoneNumber = :phoneNumber",
                    Customer.class).setParameter("phoneNumber", phoneNumber.trim()).getResultList();

            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return findByPhoneNumber(phoneNumber).isPresent();
    }

    public List<Customer> findCustomers(int page, int limit, String keyword) {
        EntityManager em = getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT c FROM Customer c WHERE 1=1 ");

            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append("AND (LOWER(c.fullName) LIKE LOWER(:keyword) ")
                        .append("OR LOWER(c.phoneNumber) LIKE LOWER(:keyword) ")
                        .append("OR LOWER(c.email) LIKE LOWER(:keyword)) ");
            }

            jpql.append("ORDER BY c.createdAt DESC");

            TypedQuery<Customer> query = em.createQuery(jpql.toString(), Customer.class);

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.trim() + "%");
            }

            int offset = (page - 1) * limit;
            query.setFirstResult(offset);
            query.setMaxResults(limit);

            return query.getResultList();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public List<CustomerResponse.InvoiceHistoryDto> findCustomerInvoices(String customerId,
            int page,
            int limit,
            String status,
            LocalDateTime fromDateTime,
            LocalDateTime toDateTime) {
        EntityManager em = getEntityManager();
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT inv.invoice_id, inv.created_at, inv.status, inv.original_total, inv.discount_amount, " +
                            "e.employee_id, e.full_name " +
                            "FROM invoice inv " +
                            "LEFT JOIN employee e ON inv.employee_id = e.employee_id " +
                            "WHERE inv.customer_id = :customerId ");

            if (status != null && !status.trim().isEmpty()) {
                sql.append("AND inv.status = :status ");
            }
            if (fromDateTime != null) {
                sql.append("AND inv.created_at >= :fromDateTime ");
            }
            if (toDateTime != null) {
                sql.append("AND inv.created_at <= :toDateTime ");
            }

            sql.append("ORDER BY inv.created_at DESC");

            Query query = em.createNativeQuery(sql.toString());
            query.setParameter("customerId", customerId.trim());

            if (status != null && !status.trim().isEmpty()) {
                query.setParameter("status", status.trim().toUpperCase());
            }
            if (fromDateTime != null) {
                query.setParameter("fromDateTime", fromDateTime);
            }
            if (toDateTime != null) {
                query.setParameter("toDateTime", toDateTime);
            }

            int offset = (page - 1) * limit;
            query.setFirstResult(offset);
            query.setMaxResults(limit);

            @SuppressWarnings("unchecked")
            List<Object[]> rawList = query.getResultList();
            List<CustomerResponse.InvoiceHistoryDto> result = new ArrayList<>();

            for (Object[] row : rawList) {
                String invoiceId = row[0] != null ? row[0].toString() : null;

                LocalDateTime createdAt = null;
                if (row[1] instanceof Timestamp) {
                    createdAt = ((Timestamp) row[1]).toLocalDateTime();
                } else if (row[1] instanceof LocalDateTime) {
                    createdAt = (LocalDateTime) row[1];
                }

                String invStatus = row[2] != null ? row[2].toString() : null;

                BigDecimal originalTotal = BigDecimal.ZERO;
                if (row[3] instanceof BigDecimal) {
                    originalTotal = (BigDecimal) row[3];
                } else if (row[3] instanceof Number) {
                    originalTotal = BigDecimal.valueOf(((Number) row[3]).doubleValue());
                }

                BigDecimal discountAmount = null;
                if (row[4] instanceof BigDecimal) {
                    discountAmount = (BigDecimal) row[4];
                } else if (row[4] instanceof Number) {
                    discountAmount = BigDecimal.valueOf(((Number) row[4]).doubleValue());
                }

                String empId = row[5] != null ? row[5].toString() : null;
                String empFullName = row[6] != null ? row[6].toString() : null;

                BigDecimal nonNullDiscount = (discountAmount != null) ? discountAmount : BigDecimal.ZERO;
                BigDecimal finalTotal = originalTotal.subtract(nonNullDiscount);

                String formattedDate = (createdAt != null)
                        ? createdAt.atOffset(ZoneOffset.UTC).format(ISO_FORMATTER)
                        : null;

                CustomerResponse.EmployeeDto empDto = null;
                if (empId != null) {
                    empDto = new CustomerResponse.EmployeeDto(empId, empFullName);
                }

                result.add(new CustomerResponse.InvoiceHistoryDto(
                        invoiceId,
                        formattedDate,
                        invStatus,
                        originalTotal,
                        discountAmount,
                        finalTotal,
                        empDto));
            }

            return result;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public List<CustomerResponse.CustomerSummaryDto> findCustomerSummaries(int page, int limit, String keyword) {
        EntityManager em = getEntityManager();
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT customer_id, full_name, phone_number, email, loyalty_points, " +
                            "total_orders, total_spent, last_purchase_date, member_tier " +
                            "FROM dbo.v_tong_hop_chi_tieu_khach_hang WHERE 1=1 ");
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql.append("AND (LOWER(full_name) LIKE LOWER(:keyword) ")
                        .append("OR LOWER(phone_number) LIKE LOWER(:keyword) ")
                        .append("OR LOWER(email) LIKE LOWER(:keyword) ")
                        .append("OR LOWER(member_tier) LIKE LOWER(:keyword)) ");
            }
            sql.append("ORDER BY total_spent DESC, loyalty_points DESC");

            Query query = em.createNativeQuery(sql.toString());
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.trim() + "%");
            }

            int offset = Math.max(0, (page - 1) * limit);
            query.setFirstResult(offset);
            query.setMaxResults(limit);

            @SuppressWarnings("unchecked")
            List<Object[]> rawList = query.getResultList();
            List<CustomerResponse.CustomerSummaryDto> result = new ArrayList<>();

            for (Object[] row : rawList) {
                String customerId = row[0] != null ? row[0].toString() : null;
                String fullName = row[1] != null ? row[1].toString() : null;
                String phone = row[2] != null ? row[2].toString() : null;
                String email = row[3] != null ? row[3].toString() : null;

                Integer points = 0;
                if (row[4] instanceof Number) {
                    points = ((Number) row[4]).intValue();
                }

                Long totalOrders = 0L;
                if (row[5] instanceof Number) {
                    totalOrders = ((Number) row[5]).longValue();
                }

                BigDecimal totalSpent = BigDecimal.ZERO;
                if (row[6] instanceof BigDecimal) {
                    totalSpent = (BigDecimal) row[6];
                } else if (row[6] instanceof Number) {
                    totalSpent = BigDecimal.valueOf(((Number) row[6]).doubleValue());
                }

                String lastPurchaseDate = null;
                if (row[7] instanceof Timestamp) {
                    lastPurchaseDate = ((Timestamp) row[7]).toLocalDateTime().atOffset(ZoneOffset.UTC)
                            .format(ISO_FORMATTER);
                } else if (row[7] instanceof LocalDateTime) {
                    lastPurchaseDate = ((LocalDateTime) row[7]).atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
                } else if (row[7] != null) {
                    lastPurchaseDate = row[7].toString();
                }

                String memberTier = row[8] != null ? row[8].toString() : "THÀNH VIÊN";

                result.add(new CustomerResponse.CustomerSummaryDto(
                        customerId, fullName, phone, email, points, totalOrders, totalSpent, lastPurchaseDate,
                        memberTier));
            }
            return result;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public List<CustomerResponse.CustomerTransactionDto> findCustomerTransactions(String customerId, LocalDate fromDate,
            LocalDate toDate) {
        EntityManager em = getEntityManager();
        try {
            String sql = "SELECT transaction_id, invoice_id, amount, payment_method, payment_time, status " +
                    "FROM dbo.fn_LichSuGiaoDichKhachHang(:customerId, :fromDate, :toDate) " +
                    "ORDER BY payment_time DESC";
            Query query = em.createNativeQuery(sql);
            query.setParameter("customerId", customerId.trim());
            query.setParameter("fromDate", fromDate != null ? java.sql.Date.valueOf(fromDate) : null);
            query.setParameter("toDate", toDate != null ? java.sql.Date.valueOf(toDate) : null);

            @SuppressWarnings("unchecked")
            List<Object[]> rawList = query.getResultList();
            List<CustomerResponse.CustomerTransactionDto> result = new ArrayList<>();

            for (Object[] row : rawList) {
                String txId = row[0] != null ? row[0].toString() : null;
                String invId = row[1] != null ? row[1].toString() : null;

                BigDecimal amount = BigDecimal.ZERO;
                if (row[2] instanceof BigDecimal) {
                    amount = (BigDecimal) row[2];
                } else if (row[2] instanceof Number) {
                    amount = BigDecimal.valueOf(((Number) row[2]).doubleValue());
                }

                String method = row[3] != null ? row[3].toString() : null;

                String paymentTime = null;
                if (row[4] instanceof Timestamp) {
                    paymentTime = ((Timestamp) row[4]).toLocalDateTime().atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
                } else if (row[4] instanceof LocalDateTime) {
                    paymentTime = ((LocalDateTime) row[4]).atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
                } else if (row[4] != null) {
                    paymentTime = row[4].toString();
                }

                String status = row[5] != null ? row[5].toString() : null;

                result.add(new CustomerResponse.CustomerTransactionDto(
                        txId, invId, amount, method, paymentTime, status));
            }
            return result;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public void redeemLoyaltyPoints(String customerId, String invoiceId, int pointsToRedeem) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Query query = em
                    .createNativeQuery("EXEC dbo.sp_DoiDiemThuongKhachHang :customerId, :invoiceId, :pointsToRedeem");
            query.setParameter("customerId", customerId.trim());
            query.setParameter("invoiceId", invoiceId.trim());
            query.setParameter("pointsToRedeem", pointsToRedeem);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            Throwable cause = e;
            while (cause.getCause() != null && cause != cause.getCause()) {
                cause = cause.getCause();
            }
            String msg = cause.getMessage() != null ? cause.getMessage() : e.getMessage();
            throw new AppException.BusinessException(msg);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}
