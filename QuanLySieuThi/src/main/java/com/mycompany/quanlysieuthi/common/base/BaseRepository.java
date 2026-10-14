package com.mycompany.quanlysieuthi.common.base;

import com.mycompany.quanlysieuthi.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Generic Base Repository cung cấp các thao tác CRUD chuẩn hóa và quản lý vòng đời transaction chống rò rỉ kết nối.
 *
 * @param <T>  Kiểu dữ liệu của Entity
 * @param <ID> Kiểu dữ liệu của Khóa chính (Primary Key)
 */
public abstract class BaseRepository<T, ID> {

    protected final Class<T> entityClass;

    protected BaseRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected EntityManager getEntityManager() {
        return JpaUtil.getEntityManager();
    }

    public Optional<T> findById(ID id) {
        EntityManager em = getEntityManager();
        try {
            return Optional.ofNullable(em.find(entityClass, id));
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public List<T> findAll() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            return em.createQuery(jpql, entityClass).getResultList();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public T save(T entity) {
        return executeInTransactionWithResult(em -> {
            em.persist(entity);
            return entity;
        });
    }

    public T update(T entity) {
        return executeInTransactionWithResult(em -> em.merge(entity));
    }

    public void delete(T entity) {
        executeInTransaction(em -> {
            T managed = em.contains(entity) ? entity : em.merge(entity);
            em.remove(managed);
        });
    }

    public boolean deleteById(ID id) {
        return executeInTransactionWithResult(em -> {
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
                return true;
            }
            return false;
        });
    }

    public void executeInTransaction(Consumer<EntityManager> action) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            action.accept(em);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public <R> R executeInTransactionWithResult(Function<EntityManager, R> action) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R result = action.apply(em);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}
