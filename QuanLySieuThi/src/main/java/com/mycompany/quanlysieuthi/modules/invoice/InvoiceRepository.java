package com.mycompany.quanlysieuthi.modules.invoice;

import com.mycompany.quanlysieuthi.common.repository.BaseRepository;
import com.mycompany.quanlysieuthi.config.JpaUtil;
import com.mycompany.quanlysieuthi.entity.Customer;
import com.mycompany.quanlysieuthi.entity.Invoice;
import com.mycompany.quanlysieuthi.entity.InvoiceDetail;
import com.mycompany.quanlysieuthi.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository handling database operations for Invoice module.
 */
public class InvoiceRepository extends BaseRepository<Invoice, String> {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public InvoiceRepository() {
        super(Invoice.class);
    }

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

    public void saveInvoice(Invoice invoice, List<InvoiceDetail> details, Customer customer, Map<String, Integer> stockDeductions) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            for (Map.Entry<String, Integer> entry : stockDeductions.entrySet()) {
                Product product = em.find(Product.class, entry.getKey());
                if (product == null) {
                    throw new IllegalArgumentException("Không tìm thấy sản phẩm: " + entry.getKey());
                }
                if (product.getStockQuantity() < entry.getValue()) {
                    throw new IllegalArgumentException("Sản phẩm '" + product.getProductName() + "' không đủ số lượng tồn kho");
                }
                product.setStockQuantity(product.getStockQuantity() - entry.getValue());
                em.merge(product);
            }

            if (customer != null) {
                em.merge(customer);
            }

            em.persist(invoice);

            for (InvoiceDetail detail : details) {
                em.persist(detail);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @SuppressWarnings("unchecked")
    public List<InvoiceResponse.ListItemDto> findInvoices(Integer page, Integer limit, String status,
                                                          String fromDate, String toDate,
                                                          String customerId, String employeeId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            int pageNum = (page == null || page < 1) ? 1 : page;
            int limitNum = (limit == null || limit < 1) ? 10 : limit;
            int offset = (pageNum - 1) * limitNum;

            StringBuilder sql = new StringBuilder(
                    "SELECT inv.invoice_id, inv.created_at, inv.status, inv.original_total, inv.discount_amount, " +
                    "e.employee_id, e.full_name, " +
                    "c.customer_id, c.full_name AS customer_name, c.phone_number " +
                    "FROM invoice inv " +
                    "LEFT JOIN employee e ON inv.employee_id = e.employee_id " +
                    "LEFT JOIN customer c ON inv.customer_id = c.customer_id " +
                    "WHERE 1=1 "
            );

            if (status != null && !status.trim().isEmpty()) {
                sql.append("AND inv.status = :status ");
            }
            if (fromDate != null && !fromDate.trim().isEmpty()) {
                sql.append("AND inv.created_at >= :fromDateTime ");
            }
            if (toDate != null && !toDate.trim().isEmpty()) {
                sql.append("AND inv.created_at <= :toDateTime ");
            }
            if (customerId != null && !customerId.trim().isEmpty()) {
                sql.append("AND inv.customer_id = :customerId ");
            }
            if (employeeId != null && !employeeId.trim().isEmpty()) {
                sql.append("AND inv.employee_id = :employeeId ");
            }

            sql.append("ORDER BY inv.created_at DESC ");
            sql.append("LIMIT :limit OFFSET :offset");

            Query query = em.createNativeQuery(sql.toString());

            if (status != null && !status.trim().isEmpty()) {
                query.setParameter("status", status.trim().toUpperCase());
            }
            if (fromDate != null && !fromDate.trim().isEmpty()) {
                query.setParameter("fromDateTime", Timestamp.valueOf(fromDate.trim() + " 00:00:00"));
            }
            if (toDate != null && !toDate.trim().isEmpty()) {
                query.setParameter("toDateTime", Timestamp.valueOf(toDate.trim() + " 23:59:59"));
            }
            if (customerId != null && !customerId.trim().isEmpty()) {
                query.setParameter("customerId", customerId.trim());
            }
            if (employeeId != null && !employeeId.trim().isEmpty()) {
                query.setParameter("employeeId", employeeId.trim());
            }

            query.setParameter("limit", limitNum);
            query.setParameter("offset", offset);

            List<Object[]> rows = query.getResultList();
            List<InvoiceResponse.ListItemDto> list = new ArrayList<>();

            for (Object[] row : rows) {
                String invId = row[0] != null ? row[0].toString() : null;
                String createdAtStr = formatTimestamp(row[1]);
                String invStatus = row[2] != null ? row[2].toString() : null;
                BigDecimal originalTotal = toBigDecimal(row[3]);
                BigDecimal discountAmount = toBigDecimal(row[4]);
                BigDecimal finalAmount = originalTotal.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);

                String empId = row[5] != null ? row[5].toString() : null;
                String empName = row[6] != null ? row[6].toString() : null;
                InvoiceResponse.EmployeeDto empDto = empId != null ? new InvoiceResponse.EmployeeDto(empId, empName) : null;

                String custId = row[7] != null ? row[7].toString() : null;
                String custName = row[8] != null ? row[8].toString() : null;
                String custPhone = row[9] != null ? row[9].toString() : null;
                InvoiceResponse.CustomerDto custDto = custId != null ? new InvoiceResponse.CustomerDto(custId, custName, custPhone) : null;

                list.add(new InvoiceResponse.ListItemDto(
                        invId,
                        createdAtStr,
                        invStatus,
                        originalTotal,
                        discountAmount != null ? discountAmount : BigDecimal.ZERO,
                        finalAmount,
                        empDto,
                        custDto
                ));
            }

            return list;
        } finally {
            em.close();
        }
    }

    @SuppressWarnings("unchecked")
    public Optional<InvoiceResponse.DetailResponse> findInvoiceDetailById(String invoiceId) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String sql = "SELECT inv.invoice_id, inv.created_at, inv.status, inv.original_total, inv.discount_amount, " +
                    "e.employee_id, e.full_name, e.username, " +
                    "c.customer_id, c.full_name AS customer_name, c.phone_number, c.loyalty_points " +
                    "FROM invoice inv " +
                    "LEFT JOIN employee e ON inv.employee_id = e.employee_id " +
                    "LEFT JOIN customer c ON inv.customer_id = c.customer_id " +
                    "WHERE inv.invoice_id = :invoiceId";

            Query query = em.createNativeQuery(sql);
            query.setParameter("invoiceId", invoiceId.trim());

            List<Object[]> rows = query.getResultList();
            if (rows.isEmpty()) {
                return Optional.empty();
            }

            Object[] row = rows.get(0);
            String invId = row[0] != null ? row[0].toString() : null;
            String createdAtStr = formatTimestamp(row[1]);
            String status = row[2] != null ? row[2].toString() : null;
            BigDecimal originalTotal = toBigDecimal(row[3]);
            BigDecimal discountAmount = toBigDecimal(row[4]);
            BigDecimal finalAmount = originalTotal.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);

            String empId = row[5] != null ? row[5].toString() : null;
            String empName = row[6] != null ? row[6].toString() : null;
            String empUsername = row[7] != null ? row[7].toString() : null;
            InvoiceResponse.EmployeeDto empDto = empId != null ? new InvoiceResponse.EmployeeDto(empId, empName, empUsername) : null;

            String custId = row[8] != null ? row[8].toString() : null;
            String custName = row[9] != null ? row[9].toString() : null;
            String custPhone = row[10] != null ? row[10].toString() : null;
            Integer custPoints = row[11] != null ? ((Number) row[11]).intValue() : null;
            InvoiceResponse.CustomerDto custDto = custId != null ? new InvoiceResponse.CustomerDto(custId, custName, custPhone, custPoints) : null;

            String itemsSql = "SELECT d.invoice_detail_id, d.product_id, p.product_name, d.quantity, d.unit_price, d.subtotal " +
                    "FROM invoice_detail d " +
                    "JOIN product p ON d.product_id = p.product_id " +
                    "WHERE d.invoice_id = :invoiceId";

            Query itemsQuery = em.createNativeQuery(itemsSql);
            itemsQuery.setParameter("invoiceId", invoiceId.trim());

            List<Object[]> itemRows = itemsQuery.getResultList();
            List<InvoiceResponse.ItemDetailDto> items = new ArrayList<>();

            for (Object[] ir : itemRows) {
                String detailId = ir[0] != null ? ir[0].toString() : null;
                String productId = ir[1] != null ? ir[1].toString() : null;
                String productName = ir[2] != null ? ir[2].toString() : null;
                Integer quantity = ir[3] != null ? ((Number) ir[3]).intValue() : 0;
                BigDecimal unitPrice = toBigDecimal(ir[4]);
                BigDecimal subtotal = toBigDecimal(ir[5]);

                items.add(new InvoiceResponse.ItemDetailDto(detailId, productId, productName, quantity, unitPrice, subtotal));
            }

            return Optional.of(new InvoiceResponse.DetailResponse(
                    invId,
                    createdAtStr,
                    status,
                    originalTotal,
                    discountAmount != null ? discountAmount : BigDecimal.ZERO,
                    finalAmount,
                    empDto,
                    custDto,
                    items
            ));
        } finally {
            em.close();
        }
    }

    @SuppressWarnings("unchecked")
    public boolean cancelInvoice(String invoiceId) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Invoice invoice = em.find(Invoice.class, invoiceId.trim());
            if (invoice == null) {
                tx.rollback();
                return false;
            }

            if (!"PENDING".equalsIgnoreCase(invoice.getStatus())) {
                throw new IllegalStateException("Hóa đơn đã ở trạng thái " + invoice.getStatus() +
                        " (chỉ cho phép hủy hóa đơn PENDING).");
            }

            String detailSql = "SELECT d FROM InvoiceDetail d WHERE d.invoiceId = :invoiceId";
            List<InvoiceDetail> details = em.createQuery(detailSql, InvoiceDetail.class)
                    .setParameter("invoiceId", invoiceId.trim())
                    .getResultList();

            for (InvoiceDetail detail : details) {
                Product product = em.find(Product.class, detail.getProductId());
                if (product != null) {
                    product.setStockQuantity(product.getStockQuantity() + detail.getQuantity());
                    em.merge(product);
                }
            }

            if (invoice.getCustomerId() != null && !invoice.getCustomerId().trim().isEmpty()) {
                Customer customer = em.find(Customer.class, invoice.getCustomerId());
                if (customer != null) {
                    BigDecimal discount = invoice.getDiscountAmount();
                    if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
                        int refundPoints = (int) Math.round((discount.doubleValue() / 1000.0) * 100000.0);
                        int current = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
                        customer.setLoyaltyPoints(current + refundPoints);
                    } else {
                        int pointsEarned = (int) Math.floor(invoice.getOriginalTotal().doubleValue() / 10000.0) * 100;
                        int current = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
                        customer.setLoyaltyPoints(Math.max(0, current - pointsEarned));
                    }
                    em.merge(customer);
                }
            }

            invoice.setStatus("CANCELLED");
            em.merge(invoice);

            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(value.toString());
    }

    private static String formatTimestamp(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime().format(ISO_FORMATTER);
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(ISO_FORMATTER);
        }
        return value.toString();
    }
}
