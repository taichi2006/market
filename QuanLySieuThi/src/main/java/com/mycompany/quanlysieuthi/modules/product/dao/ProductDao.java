package com.mycompany.quanlysieuthi.modules.product.dao;

import com.mycompany.quanlysieuthi.config.JpaUtil;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.entity.Product;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.entity.ProductCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Product database operations.
 */
public class ProductDao {

    public List<Object[]> findProducts(int page, int limit, String keyword, String categoryId, String productStatus) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT p, c.categoryName " +
                    "FROM Product p " +
                    "LEFT JOIN ProductCategory c ON p.categoryId = c.categoryId " +
                    "WHERE 1=1 "
            );

            if (productStatus != null && !productStatus.trim().isEmpty()) {
                jpql.append("AND p.productStatus = :productStatus ");
            }

            if (categoryId != null && !categoryId.trim().isEmpty()) {
                jpql.append("AND p.categoryId = :categoryId ");
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append("AND LOWER(p.productName) LIKE LOWER(:keyword) ");
            }

            jpql.append("ORDER BY p.productName ASC");

            TypedQuery<Object[]> query = em.createQuery(jpql.toString(), Object[].class);

            if (productStatus != null && !productStatus.trim().isEmpty()) {
                query.setParameter("productStatus", productStatus.trim());
            }

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
            em.close();
        }
    }

    public Optional<Object[]> findByIdWithCategory(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT p, c.categoryName " +
                    "FROM Product p " +
                    "LEFT JOIN ProductCategory c ON p.categoryId = c.categoryId " +
                    "WHERE p.productId = :productId";
            List<Object[]> rows = em.createQuery(jpql, Object[].class)
                    .setParameter("productId", productId.trim())
                    .getResultList();
            return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
        } finally {
            em.close();
        }
    }

    public Optional<Product> findById(String productId) {
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

    public boolean existsCategoryById(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            ProductCategory category = em.find(ProductCategory.class, categoryId.trim());
            return category != null;
        } finally {
            em.close();
        }
    }

    public Product updateProduct(Product product) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Product updated = em.merge(product);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi khi cập nhật sản phẩm: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public List<Object[]> findLowStockProducts(int threshold, String categoryId, int page, int limit) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT p, c.categoryName " +
                    "FROM Product p " +
                    "LEFT JOIN ProductCategory c ON p.categoryId = c.categoryId " +
                    "WHERE p.stockQuantity <= :threshold " +
                    "  AND p.productStatus = 'ON_SALE' "
            );

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
            em.close();
        }
    }

    public long countLowStockProducts(int threshold, String categoryId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT COUNT(p) " +
                    "FROM Product p " +
                    "WHERE p.stockQuantity <= :threshold " +
                    "  AND p.productStatus = 'ON_SALE' "
            );

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
            em.close();
        }
    }
}

