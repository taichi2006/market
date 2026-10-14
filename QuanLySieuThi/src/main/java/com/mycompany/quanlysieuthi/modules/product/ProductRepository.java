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
                    "WHERE 1=1 "
            );

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
                jpql.append("AND (LOWER(TRIM(p.productName)) LIKE LOWER(:keyword) OR LOWER(TRIM(p.productId)) LIKE LOWER(:keyword)) ");
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
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}
