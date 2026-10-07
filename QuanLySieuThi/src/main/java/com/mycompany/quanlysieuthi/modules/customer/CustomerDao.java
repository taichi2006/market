package com.mycompany.quanlysieuthi.modules.customer;

import com.mycompany.quanlysieuthi.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Customer and Invoice operations.
 */
public class CustomerDao {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public Optional<Customer> findById(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Customer customer = em.find(Customer.class, customerId.trim());
            return Optional.ofNullable(customer);
        } finally {
            em.close();
        }
    }

    public Optional<Customer> findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Customer> list = em.createQuery(
                    "SELECT c FROM Customer c WHERE c.phoneNumber = :phoneNumber",
                    Customer.class
            ).setParameter("phoneNumber", phoneNumber.trim()).getResultList();

            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            em.close();
        }
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return findByPhoneNumber(phoneNumber).isPresent();
    }

    public Customer save(Customer customer) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Customer result = em.merge(customer);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi khi lưu Customer: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public List<Customer> findCustomers(int page, int limit, String keyword) {
        EntityManager em = JpaUtil.getEntityManager();
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
            em.close();
        }
    }

    public List<CustomerInvoiceHistoryDto> findCustomerInvoices(String customerId,
                                                               int page,
                                                               int limit,
                                                               String status,
                                                               LocalDateTime fromDateTime,
                                                               LocalDateTime toDateTime) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT inv.invoice_id, inv.created_at, inv.status, inv.original_total, inv.discount_amount, " +
                    "e.employee_id, e.full_name " +
                    "FROM invoice inv " +
                    "LEFT JOIN employee e ON inv.employee_id = e.employee_id " +
                    "WHERE inv.customer_id = :customerId "
            );

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
            List<CustomerInvoiceHistoryDto> result = new ArrayList<>();

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

                CustomerInvoiceEmployeeDto empDto = null;
                if (empId != null) {
                    empDto = new CustomerInvoiceEmployeeDto(empId, empFullName);
                }

                result.add(new CustomerInvoiceHistoryDto(
                        invoiceId,
                        formattedDate,
                        invStatus,
                        originalTotal,
                        discountAmount,
                        finalTotal,
                        empDto
                ));
            }

            return result;
        } finally {
            em.close();
        }
    }
}
