package com.mycompany.quanlysieuthi.position;

import com.mycompany.quanlysieuthi.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Position entity using JPA EntityManager.
 */
public class PositionDao {

    public List<Position> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Position p ORDER BY p.id ASC", Position.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public Optional<Position> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Position position = em.find(Position.class, id);
            return Optional.ofNullable(position);
        } finally {
            em.close();
        }
    }

    public Position save(Position position) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Position existing = em.find(Position.class, position.getId());
            Position result;
            if (existing == null) {
                em.persist(position);
                result = position;
            } else {
                result = em.merge(position);
            }
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to persist Position: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public boolean deleteById(String id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Position position = em.find(Position.class, id);
            if (position != null) {
                em.remove(position);
                tx.commit();
                return true;
            }
            tx.commit();
            return false;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to delete Position: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
