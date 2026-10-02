package com.mycompany.quanlysieuthi.employee;

import com.mycompany.quanlysieuthi.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Employee entity using JPA EntityManager.
 */
public class EmployeeDao {

    public List<Employee> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM Employee e LEFT JOIN FETCH e.position ORDER BY e.id ASC",
                    Employee.class
            ).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public Optional<Employee> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Employee> list = em.createQuery(
                    "SELECT e FROM Employee e LEFT JOIN FETCH e.position WHERE e.id = :id",
                    Employee.class
            ).setParameter("id", id).getResultList();

            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            em.close();
        }
    }

    public Employee save(Employee employee) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee result = em.merge(employee);
            if (result.getPosition() != null) {
                // Eagerly initialize proxy before closing EntityManager
                result.getPosition().getName();
            }
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to persist Employee: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public boolean deleteById(String id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee employee = em.find(Employee.class, id);
            if (employee != null) {
                em.remove(employee);
                tx.commit();
                return true;
            }
            tx.commit();
            return false;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to delete Employee: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
