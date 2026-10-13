package com.mycompany.quanlysieuthi.modules.category;

import com.mycompany.quanlysieuthi.common.repository.BaseRepository;
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
                jpql.append("AND (LOWER(c.categoryId) LIKE LOWER(:keyword) OR LOWER(c.categoryName) LIKE LOWER(:keyword)) ");
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
                jpql.append("AND (LOWER(c.categoryId) LIKE LOWER(:keyword) OR LOWER(c.categoryName) LIKE LOWER(:keyword)) ");
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
}
