package com.mycompany.quanlysieuthi.modules.product;

import com.mycompany.quanlysieuthi.common.base.BaseRepository;
import com.mycompany.quanlysieuthi.entity.Product;
import com.mycompany.quanlysieuthi.entity.ProductCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Repository handling database operations for Product entity.
 */
public class ProductRepository extends BaseRepository<Product, String> {

    public ProductRepository() {
        super(Product.class);
    }

    public Product updateProduct(Product product) {
        return update(product);
    }

    public List<Object[]> findProducts(int page, int limit, String keyword, String categoryId, String productStatus) {
        EntityManager em = getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT p, c.categoryName " +
                            "FROM Product p " +
                            "LEFT JOIN ProductCategory c ON p.categoryId = c.categoryId " +
                            "WHERE 1=1 ");

            if (productStatus != null && !productStatus.trim().isEmpty()) {
                String normalizedStatus = productStatus.trim().toUpperCase();
                if ("ON_SALE".equals(normalizedStatus)) {
                    jpql.append("AND (UPPER(TRIM(p.productStatus)) = 'ON_SALE' OR p.productStatus IS NULL) ");
                } else if ("DISCONTINUED".equals(normalizedStatus)) {
                    jpql.append("AND UPPER(TRIM(p.productStatus)) = 'DISCONTINUED' ");
                }
            }
            if (categoryId != null && !categoryId.trim().isEmpty()) {
                jpql.append("AND p.categoryId = :categoryId ");
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(
                        "AND (LOWER(TRIM(p.productName)) LIKE LOWER(:keyword) OR LOWER(TRIM(p.productId)) LIKE LOWER(:keyword)) ");
            }

            jpql.append("ORDER BY p.productName ASC");

            TypedQuery<Object[]> query = em.createQuery(jpql.toString(), Object[].class);

            if (categoryId != null && !categoryId.trim().isEmpty()) {
                query.setParameter("categoryId", categoryId.trim());
            }
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

    public Optional<Object[]> findByIdWithCategory(String productId) {
        return findProductWithCategoryById(productId);
    }

    public Optional<Object[]> findProductWithCategoryById(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT p, c.categoryName " +
                    "FROM Product p " +
                    "LEFT JOIN ProductCategory c ON p.categoryId = c.categoryId " +
                    "WHERE p.productId = :productId";

            List<Object[]> results = em.createQuery(jpql, Object[].class)
                    .setParameter("productId", productId.trim())
                    .getResultList();

            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public boolean existsCategoryById(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return false;
        }
        EntityManager em = getEntityManager();
        try {
            ProductCategory category = em.find(ProductCategory.class, categoryId.trim());
            return category != null;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public List<Object[]> findLowStockProducts(int threshold, String categoryId, int page, int limit) {
        EntityManager em = getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT p, c.categoryName " +
                            "FROM Product p " +
                            "LEFT JOIN ProductCategory c ON p.categoryId = c.categoryId " +
                            "WHERE p.stockQuantity <= :threshold " +
                            "  AND p.productStatus = 'ON_SALE' ");

            if (categoryId != null && !categoryId.trim().isEmpty()) {
                jpql.append("AND p.categoryId = :categoryId ");
            }

            jpql.append("ORDER BY p.stockQuantity ASC, p.productName ASC");

            TypedQuery<Object[]> query = em.createQuery(jpql.toString(), Object[].class);
            query.setParameter("threshold", threshold);

            if (categoryId != null && !categoryId.trim().isEmpty()) {
                query.setParameter("categoryId", categoryId.trim());
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

    public long countLowStockProducts(int threshold, String categoryId) {
        EntityManager em = getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT COUNT(p) " +
                            "FROM Product p " +
                            "WHERE p.stockQuantity <= :threshold " +
                            "  AND p.productStatus = 'ON_SALE' ");

            if (categoryId != null && !categoryId.trim().isEmpty()) {
                jpql.append("AND p.categoryId = :categoryId ");
            }

            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            query.setParameter("threshold", threshold);

            if (categoryId != null && !categoryId.trim().isEmpty()) {
                query.setParameter("categoryId", categoryId.trim());
            }

            Long count = query.getSingleResult();
            return count != null ? count : 0L;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Đánh giá mức độ rủi ro kho của một mặt hàng cụ thể thông qua Scalar Function
     * dbo.fn_EvaluateProductStockRisk.
     * Trả về các nhãn rủi ro: 'HẾT HÀNG TRONG KHO', 'ĐÃ HẾT HẠN SỬ DỤNG - CẦN HỦY',
     * 'CẬN HẠN KHẨN CẤP (<= 15 NGÀY)', 'TỒN KHO THẤP CẦN NHẬP BỔ SUNG', 'AN TOÀN
     * BÌNH THƯỜNG'.
     */
    public String evaluateProductStockRisk(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return "Mã sản phẩm không hợp lệ";
        }
        EntityManager em = getEntityManager();
        try {
            jakarta.persistence.Query query = em
                    .createNativeQuery("SELECT dbo.fn_EvaluateProductStockRisk(:productId)");
            query.setParameter("productId", productId.trim());
            Object result = query.getSingleResult();
            return result != null ? result.toString() : "";
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Lấy danh sách sản phẩm sắp hết hạn trong vòng daysThreshold ngày qua
     * Table-Valued Function dbo.fn_GetExpiringProductsByDays.
     */
    public List<ProductResponse.ExpiringProductDto> findExpiringProducts(int daysThreshold) {
        EntityManager em = getEntityManager();
        try {
            String sql = "SELECT product_id, product_name, category_name, stock_quantity, unit_price, " +
                    "expiration_date, days_until_expiration, potential_loss_value, risk_status " +
                    "FROM dbo.fn_GetExpiringProductsByDays(:threshold) " +
                    "ORDER BY days_until_expiration ASC, potential_loss_value DESC";

            jakarta.persistence.Query query = em.createNativeQuery(sql);
            query.setParameter("threshold", daysThreshold);

            List<?> rows = query.getResultList();
            List<ProductResponse.ExpiringProductDto> list = new java.util.ArrayList<>();
            java.time.format.DateTimeFormatter isoFormatter = java.time.format.DateTimeFormatter
                    .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            for (Object obj : rows) {
                Object[] r = (Object[]) obj;
                String pId = (String) r[0];
                String pName = (String) r[1];
                String cName = (String) r[2];
                Integer stock = r[3] != null ? ((Number) r[3]).intValue() : 0;
                java.math.BigDecimal price = r[4] != null ? new java.math.BigDecimal(r[4].toString())
                        : java.math.BigDecimal.ZERO;

                String expDateStr = null;
                if (r[5] instanceof java.time.LocalDateTime) {
                    expDateStr = ((java.time.LocalDateTime) r[5]).atOffset(java.time.ZoneOffset.UTC)
                            .format(isoFormatter);
                } else if (r[5] != null) {
                    expDateStr = r[5].toString();
                }

                Integer days = r[6] != null ? ((Number) r[6]).intValue() : 0;
                java.math.BigDecimal lossVal = r[7] != null ? new java.math.BigDecimal(r[7].toString())
                        : java.math.BigDecimal.ZERO;
                String risk = (String) r[8];

                list.add(new ProductResponse.ExpiringProductDto(
                        pId, pName, cName != null ? cName : "", stock, price, expDateStr, days, lossVal, risk));
            }
            return list;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Điều chỉnh số lượng tồn kho (tăng/giảm) an toàn thông qua Stored Procedure
     * sp_AdjustProductStockWithAudit.
     * Sử dụng UPDLOCK, ROWLOCK và kiểm tra không để âm kho tại mức CSDL.
     */
    public int adjustProductStockWithAudit(String productId, int quantityChange, String reason) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sản phẩm không được để trống.");
        }
        EntityManager em = getEntityManager();
        jakarta.persistence.EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            jakarta.persistence.StoredProcedureQuery sp = em
                    .createStoredProcedureQuery("dbo.sp_AdjustProductStockWithAudit");
            sp.registerStoredProcedureParameter("ProductId", String.class, jakarta.persistence.ParameterMode.IN);
            sp.registerStoredProcedureParameter("QuantityChange", Integer.class, jakarta.persistence.ParameterMode.IN);
            sp.registerStoredProcedureParameter("Reason", String.class, jakarta.persistence.ParameterMode.IN);
            sp.registerStoredProcedureParameter("UpdatedStock", Integer.class, jakarta.persistence.ParameterMode.OUT);

            sp.setParameter("ProductId", productId.trim());
            sp.setParameter("QuantityChange", quantityChange);
            sp.setParameter("Reason", reason != null ? reason.trim() : "Điều chỉnh kiểm kê kho");

            sp.execute();
            Integer updatedStock = (Integer) sp.getOutputParameterValue("UpdatedStock");
            tx.commit();
            return updatedStock != null ? updatedStock : 0;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            throw new RuntimeException("Lỗi điều chỉnh tồn kho: " + root.getMessage(), e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Trừ tồn kho chống tranh chấp đồng thời (Pessimistic Locking WITH (UPDLOCK,
     * ROWLOCK, HOLDLOCK))
     * thông qua Stored Procedure dbo.sp_SafeDeductProductStock.
     */
    public void safeDeductProductStock(String productId, int deductQuantity) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sản phẩm không được để trống.");
        }
        EntityManager em = getEntityManager();
        jakarta.persistence.EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            jakarta.persistence.StoredProcedureQuery sp = em
                    .createStoredProcedureQuery("dbo.sp_SafeDeductProductStock");
            sp.registerStoredProcedureParameter("ProductId", String.class, jakarta.persistence.ParameterMode.IN);
            sp.registerStoredProcedureParameter("DeductQuantity", Integer.class, jakarta.persistence.ParameterMode.IN);

            sp.setParameter("ProductId", productId.trim());
            sp.setParameter("DeductQuantity", deductQuantity);

            sp.execute();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            throw new RuntimeException("Lỗi trừ tồn kho an toàn: " + root.getMessage(), e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}
