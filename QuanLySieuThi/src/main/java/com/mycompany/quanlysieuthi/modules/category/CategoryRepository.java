package com.mycompany.quanlysieuthi.modules.category;

import com.mycompany.quanlysieuthi.common.base.BaseRepository;
import com.mycompany.quanlysieuthi.entity.ProductCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Repository handling database operations for ProductCategory entity.
 */
public class CategoryRepository extends BaseRepository<ProductCategory, String> {

    public CategoryRepository() {
        super(ProductCategory.class);
    }

    public Optional<ProductCategory> findByName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = getEntityManager();
        try {
            List<ProductCategory> list = em.createQuery(
                    "SELECT c FROM ProductCategory c WHERE LOWER(c.categoryName) = LOWER(:categoryName)",
                    ProductCategory.class
            ).setParameter("categoryName", categoryName.trim()).getResultList();

            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public boolean existsByName(String categoryName, String excludeCategoryId) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return false;
        }
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(c) FROM ProductCategory c WHERE LOWER(c.categoryName) = LOWER(:categoryName)";
            if (excludeCategoryId != null && !excludeCategoryId.trim().isEmpty()) {
                jpql += " AND c.categoryId != :excludeId";
            }
            TypedQuery<Long> query = em.createQuery(jpql, Long.class)
                    .setParameter("categoryName", categoryName.trim());
            if (excludeCategoryId != null && !excludeCategoryId.trim().isEmpty()) {
                query.setParameter("excludeId", excludeCategoryId.trim());
            }
            return query.getSingleResult() > 0;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public List<ProductCategory> findCategories(int page, int limit, String keyword) {
        EntityManager em = getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT c FROM ProductCategory c WHERE 1=1 ");
            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append("AND (LOWER(TRIM(c.categoryId)) LIKE LOWER(:keyword) OR LOWER(TRIM(c.categoryName)) LIKE LOWER(:keyword)) ");
            }
            jpql.append("ORDER BY c.categoryId ASC");

            TypedQuery<ProductCategory> query = em.createQuery(jpql.toString(), ProductCategory.class);
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.trim() + "%");
            }
            if (page > 0 && limit > 0) {
                query.setFirstResult((page - 1) * limit);
                query.setMaxResults(limit);
            }
            return query.getResultList();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public long countCategories(String keyword) {
        EntityManager em = getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT COUNT(c) FROM ProductCategory c WHERE 1=1 ");
            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append("AND (LOWER(TRIM(c.categoryId)) LIKE LOWER(:keyword) OR LOWER(TRIM(c.categoryName)) LIKE LOWER(:keyword)) ");
            }
            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.trim() + "%");
            }
            return query.getSingleResult();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public long countProductsByCategoryId(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return 0;
        }
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(p) FROM Product p WHERE p.categoryId = :catId", Long.class)
                    .setParameter("catId", categoryId.trim())
                    .getSingleResult();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Truy vấn thông tin danh mục kèm theo số lượng mặt hàng và thống kê tồn kho từ View vw_ProductCategoryInventorySummary.
     * Giải quyết triệt để vấn đề N+1 query trong Java.
     */
    public List<CategoryResponse> findCategorySummaries(int page, int limit, String keyword) {
        EntityManager em = getEntityManager();
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT category_id, category_name, total_products, total_stock_quantity, " +
                    "total_inventory_value, low_stock_product_count, expiring_soon_product_count " +
                    "FROM dbo.vw_ProductCategoryInventorySummary WHERE 1=1 "
            );
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql.append("AND (LOWER(category_id) LIKE :kw OR LOWER(category_name) LIKE :kw) ");
            }
            sql.append("ORDER BY category_id ASC");

            jakarta.persistence.Query q = em.createNativeQuery(sql.toString());
            if (keyword != null && !keyword.trim().isEmpty()) {
                q.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
            }
            if (page > 0 && limit > 0) {
                q.setFirstResult((page - 1) * limit);
                q.setMaxResults(limit);
            }
            List<?> rows = q.getResultList();
            List<CategoryResponse> list = new java.util.ArrayList<>();
            for (Object obj : rows) {
                Object[] r = (Object[]) obj;
                String id = (String) r[0];
                String name = (String) r[1];
                long totalProducts = r[2] != null ? ((Number) r[2]).longValue() : 0L;
                CategoryResponse resp = new CategoryResponse(id, name, totalProducts);
                if (r[3] != null) resp.setTotalStockQuantity(((Number) r[3]).intValue());
                if (r[4] != null) resp.setTotalInventoryValue(new java.math.BigDecimal(r[4].toString()));
                if (r[5] != null) resp.setLowStockProductCount(((Number) r[5]).intValue());
                if (r[6] != null) resp.setExpiringSoonProductCount(((Number) r[6]).intValue());
                list.add(resp);
            }
            return list;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public List<CategoryResponse> findAllCategorySummaries() {
        return findCategorySummaries(0, 0, null);
    }

    public Optional<CategoryResponse> findCategorySummaryById(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = getEntityManager();
        try {
            String sql = "SELECT category_id, category_name, total_products, total_stock_quantity, " +
                         "total_inventory_value, low_stock_product_count, expiring_soon_product_count " +
                         "FROM dbo.vw_ProductCategoryInventorySummary WHERE category_id = :id";
            jakarta.persistence.Query q = em.createNativeQuery(sql);
            q.setParameter("id", categoryId.trim());
            List<?> rows = q.getResultList();
            if (rows.isEmpty()) {
                return Optional.empty();
            }
            Object[] r = (Object[]) rows.get(0);
            String id = (String) r[0];
            String name = (String) r[1];
            long totalProducts = r[2] != null ? ((Number) r[2]).longValue() : 0L;
            CategoryResponse resp = new CategoryResponse(id, name, totalProducts);
            if (r[3] != null) resp.setTotalStockQuantity(((Number) r[3]).intValue());
            if (r[4] != null) resp.setTotalInventoryValue(new java.math.BigDecimal(r[4].toString()));
            if (r[5] != null) resp.setLowStockProductCount(((Number) r[5]).intValue());
            if (r[6] != null) resp.setExpiringSoonProductCount(((Number) r[6]).intValue());
            return Optional.of(resp);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Gộp 2 danh mục thông qua Stored Procedure sp_MergeProductCategories trong SQL.
     */
    public void mergeCategories(String sourceCategoryId, String targetCategoryId) {
        EntityManager em = getEntityManager();
        jakarta.persistence.EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            jakarta.persistence.Query query = em.createNativeQuery(
                    "EXEC dbo.sp_MergeProductCategories @SourceCategoryId = :src, @TargetCategoryId = :target"
            );
            query.setParameter("src", sourceCategoryId.trim());
            query.setParameter("target", targetCategoryId.trim());
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            throw new RuntimeException("Lỗi khi gộp danh mục: " + root.getMessage(), e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Xóa danh mục bằng SQL native để kích hoạt INSTEAD OF DELETE Trigger dbo.trg_PreventDeleteCategoryWithProducts.
     * Trigger trong database sẽ kiểm tra:
     * - Nếu còn hàng đang kinh doanh (ON_SALE) hoặc còn tồn kho (>0): RAISERROR và ROLLBACK.
     * - Nếu sản phẩm đã ngừng kinh doanh và hết tồn kho: Gỡ liên kết category_id = NULL và xóa danh mục an toàn.
     */
    public void deleteCategoryWithTrigger(String categoryId) {
        EntityManager em = getEntityManager();
        jakarta.persistence.EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            jakarta.persistence.Query query = em.createNativeQuery("DELETE FROM product_category WHERE category_id = :id");
            query.setParameter("id", categoryId.trim());
            int affected = query.executeUpdate();
            if (affected == 0) {
                throw new com.mycompany.quanlysieuthi.common.exception.AppException.NotFoundException(
                        "Không tìm thấy loại sản phẩm với mã: " + categoryId);
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
            String msg = root.getMessage();
            if (msg != null && msg.contains("Không thể xóa danh mục này")) {
                throw new com.mycompany.quanlysieuthi.common.exception.AppException.ConflictException(
                        "Không thể xóa danh mục này vì vẫn còn sản phẩm đang kinh doanh hoặc còn tồn kho!");
            }
            if (e instanceof com.mycompany.quanlysieuthi.common.exception.AppException) {
                throw (com.mycompany.quanlysieuthi.common.exception.AppException) e;
            }
            throw new RuntimeException("Lỗi khi xóa loại sản phẩm: " + msg, e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}
