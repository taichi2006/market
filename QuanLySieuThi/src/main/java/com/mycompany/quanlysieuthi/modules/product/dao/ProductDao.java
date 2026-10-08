package com.mycompany.quanlysieuthi.modules.product.dao;

import com.mycompany.quanlysieuthi.config.JpaUtil;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.entity.Product;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.entity.ProductCategory;
import jakarta.persistence.EntityManager;
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
}

