package com.mycompany.quanlysieuthi.modules.employee;

import com.mycompany.quanlysieuthi.common.repository.BaseRepository;
import com.mycompany.quanlysieuthi.entity.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Repository handling database operations for Employee entity.
 */
public class EmployeeRepository extends BaseRepository<Employee, String> {

    public EmployeeRepository() {
        super(Employee.class);
    }

    public Optional<Employee> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = getEntityManager();
        try {
            List<Employee> list = em.createQuery(
                    "SELECT e FROM Employee e WHERE e.username = :username",
                    Employee.class
            ).setParameter("username", username.trim()).getResultList();

            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public Optional<Employee> findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = getEntityManager();
        try {
            List<Employee> list = em.createQuery(
                    "SELECT e FROM Employee e WHERE e.phoneNumber = :phoneNumber",
                    Employee.class
            ).setParameter("phoneNumber", phoneNumber.trim()).getResultList();

            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
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
            List<Employee> list = em.createQuery(
                    "SELECT e FROM Employee e WHERE e.refreshToken = :refreshToken",
                    Employee.class
            ).setParameter("refreshToken", refreshToken.trim()).getResultList();

            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public Optional<Employee> findByPhoneNumberExcludingId(String phoneNumber, String employeeId) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty() || employeeId == null || employeeId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = getEntityManager();
        try {
            List<Employee> list = em.createQuery(
                    "SELECT e FROM Employee e WHERE e.phoneNumber = :phoneNumber AND e.employeeId <> :employeeId",
                    Employee.class
            ).setParameter("phoneNumber", phoneNumber.trim())
             .setParameter("employeeId", employeeId.trim())
             .getResultList();

            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public boolean softDelete(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return false;
        }
        return executeInTransactionWithResult(em -> {
            Employee employee = em.find(Employee.class, employeeId.trim());
            if (employee != null) {
                employee.setStatus(false);
                em.merge(employee);
                return true;
            }
            return false;
        });
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
        EntityManager em = getEntityManager();
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
            TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
            params.forEach(countQuery::setParameter);
            Long totalCount = countQuery.getSingleResult();
            long totalElements = (totalCount != null) ? totalCount : 0L;

            // Data query
            String dataJpql = "SELECT e FROM Employee e " + whereClause + " ORDER BY e.fullName ASC";
            TypedQuery<Employee> dataQuery = em.createQuery(dataJpql, Employee.class);
            params.forEach(dataQuery::setParameter);

            int offset = Math.max(0, (page - 1) * limit);
            dataQuery.setFirstResult(offset);
            dataQuery.setMaxResults(limit);

            List<Employee> items = dataQuery.getResultList();
            return new PageResult<>(items, totalElements);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public void updateRefreshToken(String employeeId, String refreshToken) {
        executeInTransaction(em -> {
            Employee employee = em.find(Employee.class, employeeId);
            if (employee != null) {
                employee.setRefreshToken(refreshToken);
                em.merge(employee);
            }
        });
    }
}
