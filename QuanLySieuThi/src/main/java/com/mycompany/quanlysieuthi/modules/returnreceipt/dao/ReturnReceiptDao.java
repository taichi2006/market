package com.mycompany.quanlysieuthi.modules.returnreceipt.dao;

import com.mycompany.quanlysieuthi.config.JpaUtil;
import com.mycompany.quanlysieuthi.modules.customer.Customer;
import com.mycompany.quanlysieuthi.modules.customer.Invoice;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.entity.Product;
import com.mycompany.quanlysieuthi.modules.returnreceipt.entity.ReturnReceipt;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object for Return Receipt operations.
 */
public class ReturnReceiptDao {

    /**
     * Find invoice by ID.
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
     * Find customer by ID.
     */
    public Optional<Customer> findCustomerById(String customerId) {
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

    /**
     * Find product by ID.
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
     * Find all items in an invoice (from invoice_detail table).
     * Returns list of rows: [0] product_id, [1] quantity, [2] unit_price.
     */
    public List<Object[]> findInvoiceItems(String invoiceId) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        EntityManager em = JpaUtil.getEntityManager();
        try {
            String sql = "SELECT product_id, quantity, unit_price FROM invoice_detail WHERE invoice_id = :invoiceId";
            TypedQuery<Object[]> query = em.unwrap(Session.class)
                    .createNativeQuery(sql, Object[].class);
            query.setParameter("invoiceId", invoiceId.trim());

            List<Object[]> rows = query.getResultList();
            return rows;
        } finally {
            em.close();
        }
    }

    public boolean existsReturnReceiptByInvoiceId(String invoiceId) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT COUNT(r) FROM ReturnReceipt r WHERE r.invoiceId = :invoiceId";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("invoiceId", invoiceId.trim());
            Long count = query.getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    public void saveReturnReceiptTransaction(ReturnReceipt receipt,
            Map<String, Integer> productRestockMap,
            String customerId,
            int pointsDeducted) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // 1. Insert return receipt
            em.persist(receipt);

            // 2. Restock products
            for (Map.Entry<String, Integer> entry : productRestockMap.entrySet()) {
                String productId = entry.getKey();
                int restockQty = entry.getValue();

                Product product = em.find(Product.class, productId);
                if (product != null) {
                    int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                    product.setStockQuantity(currentStock + restockQty);
                    em.merge(product);
                }
            }

            // 4. Deduct customer points if applicable
            if (customerId != null && !customerId.trim().isEmpty() && pointsDeducted > 0) {
                Customer customer = em.find(Customer.class, customerId.trim());
                if (customer != null) {
                    int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
                    customer.setLoyaltyPoints(Math.max(0, currentPoints - pointsDeducted));
                    em.merge(customer);
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
            throw new RuntimeException("Lỗi giao dịch khi lưu phiếu trả hàng: " + root.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Find return receipts for list endpoint with filters and pagination.
     */
    public List<Object[]> findReturnReceipts(int page, int limit, String invoiceId, String employeeId,
            LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT r.returnReceiptId, r.invoiceId, r.refundAmount, r.reason, r.createdAt, " +
                            "e.employeeId, e.fullName " +
                            "FROM ReturnReceipt r " +
                            "LEFT JOIN Employee e ON r.employeeId = e.employeeId " +
                            "WHERE 1=1 ");

            if (invoiceId != null && !invoiceId.trim().isEmpty()) {
                jpql.append("AND r.invoiceId = :invoiceId ");
            }
            if (employeeId != null && !employeeId.trim().isEmpty()) {
                jpql.append("AND r.employeeId = :employeeId ");
            }
            if (fromDateTime != null) {
                jpql.append("AND r.createdAt >= :fromDateTime ");
            }
            if (toDateTime != null) {
                jpql.append("AND r.createdAt <= :toDateTime ");
            }

            jpql.append("ORDER BY r.createdAt DESC");

            TypedQuery<Object[]> query = em.createQuery(jpql.toString(), Object[].class);

            if (invoiceId != null && !invoiceId.trim().isEmpty()) {
                query.setParameter("invoiceId", invoiceId.trim());
            }
            if (employeeId != null && !employeeId.trim().isEmpty()) {
                query.setParameter("employeeId", employeeId.trim());
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

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find return receipt header info with joined employee, invoice and customer.
     */
    public Optional<Object[]> findReturnReceiptHeaderById(String returnReceiptId) {
        if (returnReceiptId == null || returnReceiptId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT r.returnReceiptId, r.invoiceId, r.refundAmount, r.reason, r.createdAt, " +
                    "e.employeeId, e.fullName, e.username, " +
                    "c.customerId, c.fullName, c.phoneNumber " +
                    "FROM ReturnReceipt r " +
                    "LEFT JOIN Employee e ON r.employeeId = e.employeeId " +
                    "LEFT JOIN Invoice i ON r.invoiceId = i.invoiceId " +
                    "LEFT JOIN Customer c ON i.customerId = c.customerId " +
                    "WHERE r.returnReceiptId = :returnReceiptId";

            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setParameter("returnReceiptId", returnReceiptId.trim());

            List<Object[]> list = query.getResultList();
            if (list.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(list.get(0));
        } finally {
            em.close();
        }
    }
}
