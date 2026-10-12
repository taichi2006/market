package com.mycompany.quanlysieuthi.modules.auth;

import com.mycompany.quanlysieuthi.common.repository.BaseRepository;
import com.mycompany.quanlysieuthi.entity.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;

/**
 * Repository handling database operations for Authentication.
 */
public class AuthRepository extends BaseRepository<Employee, String> {

    public AuthRepository() {
        super(Employee.class);
    }

    public Optional<Employee> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = getEntityManager();
        try {
            Employee emp = em.createQuery(
                    "SELECT e FROM Employee e WHERE LOWER(e.username) = LOWER(:username)", Employee.class)
                    .setParameter("username", username.trim())
                    .getSingleResult();
            return Optional.of(emp);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public Optional<Employee> findByRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = getEntityManager();
        try {
            Employee emp = em.createQuery(
                    "SELECT e FROM Employee e WHERE e.refreshToken = :refreshToken", Employee.class)
                    .setParameter("refreshToken", refreshToken.trim())
                    .getSingleResult();
            return Optional.of(emp);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public void updateRefreshToken(String employeeId, String refreshToken) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return;
        }
        executeInTransaction(em -> {
            Employee emp = em.find(Employee.class, employeeId.trim());
            if (emp != null) {
                emp.setRefreshToken(refreshToken);
                em.merge(emp);
            }
        });
    }
}
