package com.mycompany.quanlysieuthi.modules.purchasereceipt;

import com.mycompany.quanlysieuthi.common.base.BaseRepository;
import com.mycompany.quanlysieuthi.entity.Product;
import com.mycompany.quanlysieuthi.entity.ProductCategory;
import com.mycompany.quanlysieuthi.entity.PurchaseReceipt;
import com.mycompany.quanlysieuthi.entity.PurchaseReceiptDetail;
import com.mycompany.quanlysieuthi.entity.Voucher;
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
 * Repository xử lý dữ liệu cho module Phiếu nhập hàng (Purchase Receipt),
 * tận dụng View và Trigger CSDL để tối ưu hóa truy vấn và cập nhật kho.
 */
public class PurchaseReceiptRepository extends BaseRepository<PurchaseReceipt, String> {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public PurchaseReceiptRepository() {
        super(PurchaseReceipt.class);
    }

    public boolean existsCategory(String categoryId) {
        if (categoryId == null || categoryId.isBlank())
            return false;
        return query(em -> em.find(ProductCategory.class, categoryId.trim()) != null);
    }

    public String findFirstCategoryId() {
        return query(em -> em
                .createQuery("SELECT c.categoryId FROM ProductCategory c ORDER BY c.categoryId ASC", String.class)
                .setMaxResults(1).getResultStream().findFirst().orElse(null));
    }

    public Optional<Product> findProductById(String productId) {
        if (productId == null || productId.isBlank())
            return Optional.empty();
        return query(em -> Optional.ofNullable(em.find(Product.class, productId.trim())));
    }

    public void savePurchaseReceiptTransaction(Voucher voucher,
            PurchaseReceipt purchaseReceipt,
            List<Product> products,
            List<PurchaseReceiptDetail> details) {
        try {
            executeInTransaction(em -> {
                em.persist(voucher);
                em.persist(purchaseReceipt);
                products.forEach(em::persist);
                details.forEach(em::persist);
            });
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null)
                root = root.getCause();
            throw new RuntimeException("Lỗi giao dịch khi lưu phiếu nhập hàng: " + root.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<PurchaseReceiptResponse.ListItemDto> findPurchaseReceipts(int page, int limit,
            String supplierName,
            String employeeId,
            LocalDateTime fromDateTime,
            LocalDateTime toDateTime) {
        return query(em -> {
            StringBuilder sql = new StringBuilder(
                    "SELECT voucher_id, supplier_name, total_amount, created_at, "
                            + "employee_id, employee_name, employee_username "
                            + "FROM v_purchase_receipt_summary WHERE 1=1 ");
            Map<String, Object> params = new HashMap<>();

            if (supplierName != null && !supplierName.isBlank()) {
                sql.append("AND LOWER(supplier_name) LIKE LOWER(:supplierName) ");
                params.put("supplierName", "%" + supplierName.trim() + "%");
            }
            if (employeeId != null && !employeeId.isBlank()) {
                sql.append("AND employee_id = :employeeId ");
                params.put("employeeId", employeeId.trim());
            }
            if (fromDateTime != null) {
                sql.append("AND created_at >= :fromDateTime ");
                params.put("fromDateTime", fromDateTime);
            }
            if (toDateTime != null) {
                sql.append("AND created_at <= :toDateTime ");
                params.put("toDateTime", toDateTime);
            }

            sql.append("ORDER BY created_at DESC");
            Query query = em.createNativeQuery(sql.toString());
            params.forEach(query::setParameter);
            query.setFirstResult((page - 1) * limit);
            query.setMaxResults(limit);

            List<Object[]> rows = query.getResultList();
            List<PurchaseReceiptResponse.ListItemDto> result = new ArrayList<>(rows.size());
            for (Object[] r : rows) {
                BigDecimal total = r[2] != null ? new BigDecimal(r[2].toString()) : BigDecimal.ZERO;
                PurchaseReceiptResponse.EmployeeDto emp = r[4] != null
                        ? new PurchaseReceiptResponse.EmployeeDto((String) r[4], (String) r[5], (String) r[6])
                        : null;
                result.add(new PurchaseReceiptResponse.ListItemDto((String) r[0], (String) r[1], total, formatIso(r[3]),
                        emp));
            }
            return result;
        });
    }

    public Optional<PurchaseReceiptResponse.DetailResponse> findPurchaseReceiptDetailById(String voucherId) {
        if (voucherId == null || voucherId.isBlank())
            return Optional.empty();

        return query(em -> {
            List<Object[]> headerList = em.createQuery(
                    "SELECT pr.voucherId, pr.supplierName, v.totalAmount, v.createdAt, "
                            + "e.employeeId, e.fullName, e.username "
                            + "FROM PurchaseReceipt pr JOIN Voucher v ON pr.voucherId = v.voucherId "
                            + "LEFT JOIN Employee e ON v.employeeId = e.employeeId WHERE pr.voucherId = :voucherId",
                    Object[].class)
                    .setParameter("voucherId", voucherId.trim())
                    .getResultList();
            if (headerList.isEmpty())
                return Optional.empty();

            Object[] h = headerList.get(0);
            BigDecimal totalAmount = (BigDecimal) h[2];
            PurchaseReceiptResponse.EmployeeDto emp = h[4] != null
                    ? new PurchaseReceiptResponse.EmployeeDto((String) h[4], (String) h[5], (String) h[6])
                    : null;

            List<Object[]> itemRows = em.createQuery(
                    "SELECT d.purchaseDetailId, p.productId, p.productName, c.categoryName, "
                            + "d.purchasedQuantity, d.purchasePrice, d.expirationDate "
                            + "FROM PurchaseReceiptDetail d JOIN Product p ON d.productId = p.productId "
                            + "LEFT JOIN ProductCategory c ON p.categoryId = c.categoryId "
                            + "WHERE d.voucherId = :voucherId ORDER BY d.purchaseDetailId ASC",
                    Object[].class)
                    .setParameter("voucherId", voucherId.trim())
                    .getResultList();

            List<PurchaseReceiptResponse.ItemDetailDto> items = new ArrayList<>(itemRows.size());
            for (Object[] ir : itemRows) {
                Integer qty = (Integer) ir[4];
                BigDecimal price = (BigDecimal) ir[5];
                BigDecimal lineTotal = (qty != null && price != null) ? price.multiply(BigDecimal.valueOf(qty))
                        : BigDecimal.ZERO;
                items.add(new PurchaseReceiptResponse.ItemDetailDto(
                        (String) ir[0], (String) ir[1], (String) ir[2], (String) ir[3],
                        qty, price, lineTotal, formatIso(ir[6])));
            }

            return Optional.of(new PurchaseReceiptResponse.DetailResponse(
                    (String) h[0], (String) h[1], totalAmount, formatIso(h[3]), emp, items));
        });
    }

    private <R> R query(Function<EntityManager, R> action) {
        EntityManager em = getEntityManager();
        try {
            return action.apply(em);
        } finally {
            if (em.isOpen())
                em.close();
        }
    }

    private static String formatIso(Object val) {
        if (val == null)
            return null;
        if (val instanceof Timestamp ts)
            return ts.toLocalDateTime().atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
        if (val instanceof LocalDateTime ldt)
            return ldt.atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
        return val.toString();
    }
}
