package com.mycompany.quanlysieuthi.employee;

import com.mycompany.quanlysieuthi.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Employee entity using JPA EntityManager.
 */
public class EmployeeDao {

    public Optional<Employee> findById(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Employee employee = em.find(Employee.class, employeeId.trim());
            return Optional.ofNullable(employee);
        } finally {
            em.close();
        }
    }

    public Optional<Employee> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Employee> list = em.createQuery(
                    "SELECT e FROM Employee e WHERE e.username = :username",
                    Employee.class
            ).setParameter("username", username.trim()).getResultList();

            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            em.close();
        }
    }

    public Optional<Employee> findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Employee> list = em.createQuery(
                    "SELECT e FROM Employee e WHERE e.phoneNumber = :phoneNumber",
                    Employee.class
            ).setParameter("phoneNumber", phoneNumber.trim()).getResultList();

            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            em.close();
        }
    }

    public Optional<Employee> findByRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Employee> list = em.createQuery(
                    "SELECT e FROM Employee e WHERE e.refreshToken = :refreshToken",
                    Employee.class
            ).setParameter("refreshToken", refreshToken.trim()).getResultList();

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

    public Optional<Employee> findByPhoneNumberExcludingId(String phoneNumber, String employeeId) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty() || employeeId == null || employeeId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Employee> list = em.createQuery(
                    "SELECT e FROM Employee e WHERE e.phoneNumber = :phoneNumber AND e.employeeId <> :employeeId",
                    Employee.class
            ).setParameter("phoneNumber", phoneNumber.trim())
             .setParameter("employeeId", employeeId.trim())
             .getResultList();

            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            em.close();
        }
    }

    public boolean softDelete(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee employee = em.find(Employee.class, employeeId.trim());
            if (employee != null) {
                employee.setStatus(false);
                em.merge(employee);
                tx.commit();
                return true;
            }
            tx.commit();
            return false;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to soft delete Employee: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public static class PageResult<T> {
        private final List<T> items;
        private final long totalElements;

        public PageResult(List<T> items, long totalElements) {
            this.items = items;
            this.totalElements = totalElements;
        }

        public List<T> getItems() {
            return items;
        }

        public long getTotalElements() {
            return totalElements;
        }
    }

    public PageResult<Employee> findEmployees(int page, int limit, String keyword, String position, Boolean status) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
            java.util.Map<String, Object> params = new java.util.LinkedHashMap<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                whereClause.append(" AND (LOWER(e.fullName) LIKE :keyword OR LOWER(e.username) LIKE :keyword) ");
                params.put("keyword", "%" + keyword.trim().toLowerCase() + "%");
            }

            if (position != null && !position.trim().isEmpty()) {
                whereClause.append(" AND UPPER(e.position) = :position ");
                params.put("position", position.trim().toUpperCase());
            }

            if (status != null) {
                whereClause.append(" AND e.status = :status ");
                params.put("status", status);
            }

            // Count query
            String countJpql = "SELECT COUNT(e) FROM Employee e " + whereClause;
            jakarta.persistence.TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
            params.forEach(countQuery::setParameter);
            Long totalCount = countQuery.getSingleResult();
            long totalElements = (totalCount != null) ? totalCount : 0L;

            // Data query
            String dataJpql = "SELECT e FROM Employee e " + whereClause + " ORDER BY e.fullName ASC";
            jakarta.persistence.TypedQuery<Employee> dataQuery = em.createQuery(dataJpql, Employee.class);
            params.forEach(dataQuery::setParameter);

            int offset = Math.max(0, (page - 1) * limit);
            dataQuery.setFirstResult(offset);
            dataQuery.setMaxResults(limit);

            List<Employee> items = dataQuery.getResultList();
            return new PageResult<>(items, totalElements);
        } finally {
            em.close();
        }
    }

    public void updateRefreshToken(String employeeId, String refreshToken) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee employee = em.find(Employee.class, employeeId);
            if (employee != null) {
                employee.setRefreshToken(refreshToken);
                em.merge(employee);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to update refresh token: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}

