package com.mycompany.quanlysieuthi.modules.invoice;

import com.mycompany.quanlysieuthi.common.base.BaseRepository;
import com.mycompany.quanlysieuthi.entity.Customer;
import com.mycompany.quanlysieuthi.entity.Invoice;
import com.mycompany.quanlysieuthi.entity.InvoiceDetail;
import com.mycompany.quanlysieuthi.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

/**
 * Repository xử lý dữ liệu cho module Hóa đơn (Invoice),
 * tích hợp tối ưu với View, Function, Stored Procedure và quản lý kết nối an toàn.
 */
public class InvoiceRepository extends BaseRepository<Invoice, String> {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public InvoiceRepository() {
        super(Invoice.class);
    }

    public Optional<Product> findProductById(String productId) {
        if (productId == null || productId.isBlank()) return Optional.empty();
        return query(em -> Optional.ofNullable(em.find(Product.class, productId.trim())));
    }

    public Optional<Customer> findCustomerById(String customerId) {
        if (customerId == null || customerId.isBlank()) return Optional.empty();
        return query(em -> Optional.ofNullable(em.find(Customer.class, customerId.trim())));
    }

    public void saveInvoice(Invoice invoice, List<InvoiceDetail> details, Customer customer,
                            Map<String, Integer> stockDeductions) {
        executeInTransaction(em -> {
            for (Map.Entry<String, Integer> entry : stockDeductions.entrySet()) {
                Product product = em.find(Product.class, entry.getKey());
                if (product == null) {
                    throw new IllegalArgumentException("Không tìm thấy sản phẩm: " + entry.getKey());
                }
                if (product.getStockQuantity() < entry.getValue()) {
                    throw new IllegalArgumentException(
                            "Sản phẩm '" + product.getProductName() + "' không đủ số lượng tồn kho");
                }
                product.setStockQuantity(product.getStockQuantity() - entry.getValue());
                em.merge(product);
            }
            if (customer != null) {
                em.merge(customer);
            }
            em.persist(invoice);
            details.forEach(em::persist);
        });
    }

    @SuppressWarnings("unchecked")
    public List<InvoiceResponse.ListItemDto> findInvoices(Integer page, Integer limit, String status,
                                                         String fromDate, String toDate,
                                                         String customerId, String employeeId) {
        int pageNum = (page == null || page < 1) ? 1 : page;
        int limitNum = (limit == null || limit < 1) ? 10 : limit;

        return query(em -> {
            StringBuilder sql = new StringBuilder(
                    "SELECT invoice_id, created_at, status, original_total, discount_amount, "
                    + "employee_id, employee_name, customer_id, customer_name, customer_phone "
                    + "FROM v_invoice_summary WHERE 1=1 ");
            Map<String, Object> params = new HashMap<>();

            if (status != null && !status.isBlank()) {
                sql.append("AND status = :status ");
                params.put("status", status.trim().toUpperCase());
            }
            if (fromDate != null && !fromDate.isBlank()) {
                sql.append("AND created_at >= :fromDateTime ");
                params.put("fromDateTime", Timestamp.valueOf(fromDate.trim() + " 00:00:00"));
            }
            if (toDate != null && !toDate.isBlank()) {
                sql.append("AND created_at <= :toDateTime ");
                params.put("toDateTime", Timestamp.valueOf(toDate.trim() + " 23:59:59"));
            }
            if (customerId != null && !customerId.isBlank()) {
                sql.append("AND customer_id = :customerId ");
                params.put("customerId", customerId.trim());
            }
            if (employeeId != null && !employeeId.isBlank()) {
                sql.append("AND employee_id = :employeeId ");
                params.put("employeeId", employeeId.trim());
            }

            sql.append("ORDER BY created_at DESC");
            Query query = em.createNativeQuery(sql.toString());
            params.forEach(query::setParameter);
            query.setFirstResult((pageNum - 1) * limitNum);
            query.setMaxResults(limitNum);

            List<Object[]> rows = query.getResultList();
            List<InvoiceResponse.ListItemDto> list = new ArrayList<>(rows.size());
            for (Object[] r : rows) {
                BigDecimal orig = toBigDecimal(r[3]);
                BigDecimal disc = toBigDecimal(r[4]);
                list.add(new InvoiceResponse.ListItemDto(
                        (String) r[0], formatIso(r[1]), (String) r[2], orig, disc, orig.subtract(disc),
                        r[5] != null ? new InvoiceResponse.EmployeeDto((String) r[5], (String) r[6]) : null,
                        r[7] != null ? new InvoiceResponse.CustomerDto((String) r[7], (String) r[8], (String) r[9]) : null));
            }
            return list;
        });
    }

    @SuppressWarnings("unchecked")
    public Optional<InvoiceResponse.DetailResponse> findInvoiceDetailById(String invoiceId) {
        if (invoiceId == null || invoiceId.isBlank()) return Optional.empty();

        return query(em -> {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT invoice_id, created_at, status, original_total, discount_amount, "
                    + "employee_id, employee_name, employee_username, "
                    + "customer_id, customer_name, customer_phone, customer_loyalty_points "
                    + "FROM v_invoice_summary WHERE invoice_id = :invoiceId")
                    .setParameter("invoiceId", invoiceId.trim())
                    .getResultList();
            if (rows.isEmpty()) return Optional.empty();

            Object[] r = rows.get(0);
            BigDecimal orig = toBigDecimal(r[3]);
            BigDecimal disc = toBigDecimal(r[4]);

            List<Object[]> itemRows = em.createNativeQuery(
                    "SELECT invoice_detail_id, product_id, product_name, quantity, unit_price, subtotal "
                    + "FROM dbo.fn_GetInvoiceItemsDetail(:invoiceId)")
                    .setParameter("invoiceId", invoiceId.trim())
                    .getResultList();

            List<InvoiceResponse.ItemDetailDto> items = new ArrayList<>(itemRows.size());
            for (Object[] ir : itemRows) {
                items.add(new InvoiceResponse.ItemDetailDto(
                        (String) ir[0], (String) ir[1], (String) ir[2],
                        ir[3] != null ? ((Number) ir[3]).intValue() : 0,
                        toBigDecimal(ir[4]), toBigDecimal(ir[5])));
            }

            return Optional.of(new InvoiceResponse.DetailResponse(
                    (String) r[0], formatIso(r[1]), (String) r[2], orig, disc, orig.subtract(disc),
                    r[5] != null ? new InvoiceResponse.EmployeeDto((String) r[5], (String) r[6], (String) r[7]) : null,
                    r[8] != null ? new InvoiceResponse.CustomerDto((String) r[8], (String) r[9], (String) r[10],
                            r[11] != null ? ((Number) r[11]).intValue() : null) : null,
                    items));
        });
    }

    public boolean cancelInvoice(String invoiceId) {
        if (invoiceId == null || invoiceId.isBlank()) return false;
        try {
            return executeInTransactionWithResult(em -> {
                em.createNativeQuery("EXEC dbo.sp_CancelInvoice :invoiceId")
                        .setParameter("invoiceId", invoiceId.trim())
                        .executeUpdate();
                return true;
            });
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) root = root.getCause();
            String msg = root.getMessage();
            if (msg != null && msg.contains("Không tìm thấy")) return false;
            throw new RuntimeException(msg != null ? msg : e.getMessage(), e);
        }
    }

    public BigDecimal calculateLoyaltyDiscount(String customerId, BigDecimal originalTotal) {
        if (customerId == null || originalTotal == null) return BigDecimal.ZERO;
        try {
            return query(em -> toBigDecimal(em.createNativeQuery(
                    "SELECT dbo.fn_CalculateLoyaltyDiscount(:customerId, :originalTotal)")
                    .setParameter("customerId", customerId.trim())
                    .setParameter("originalTotal", originalTotal)
                    .getSingleResult()));
        } catch (Exception ignored) {
            return BigDecimal.ZERO;
        }
    }

    public int calculateLoyaltyPointsEarned(BigDecimal originalTotal) {
        if (originalTotal == null) return 0;
        try {
            return query(em -> {
                Object res = em.createNativeQuery(
                        "SELECT dbo.fn_CalculateLoyaltyPointsEarned(:originalTotal)")
                        .setParameter("originalTotal", originalTotal)
                        .getSingleResult();
                return res != null ? ((Number) res).intValue() : 0;
            });
        } catch (Exception ignored) {
            return 0;
        }
    }

    private <R> R query(Function<EntityManager, R> action) {
        EntityManager em = getEntityManager();
        try {
            return action.apply(em);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    private static BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        return val instanceof BigDecimal bd ? bd : new BigDecimal(val.toString());
    }

    private static String formatIso(Object val) {
        if (val == null) return null;
        if (val instanceof Timestamp ts) return ts.toLocalDateTime().atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
        if (val instanceof LocalDateTime ldt) return ldt.atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
        return val.toString();
    }
}
