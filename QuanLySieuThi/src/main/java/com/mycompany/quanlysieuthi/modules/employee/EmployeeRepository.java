package com.mycompany.quanlysieuthi.modules.employee;

import com.mycompany.quanlysieuthi.common.base.BaseRepository;
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
                    "SELECT e FROM Employee e WHERE e.phoneNumber = :phoneNumber AND UPPER(TRIM(e.employeeId)) <> UPPER(TRIM(:employeeId))",
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

    public Optional<Employee> findByIdIgnoreCase(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return Optional.empty();
        }
        String cleanId = employeeId.trim();
        Optional<Employee> exact = findById(cleanId);
        if (exact.isPresent()) {
            return exact;
        }

        EntityManager em = getEntityManager();
        try {
            List<Employee> list = em.createQuery(
                    "SELECT e FROM Employee e WHERE UPPER(e.employeeId) = UPPER(:id)",
                    Employee.class
            ).setParameter("id", cleanId).setMaxResults(1).getResultList();

            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public long countActiveStoreOwners() {
        EntityManager em = getEntityManager();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(e) FROM Employee e WHERE UPPER(e.position) = 'STORE_OWNER' AND (e.status IS NULL OR e.status = TRUE)",
                    Long.class
            ).getSingleResult();
            return count != null ? count : 0L;
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
        EntityManager em = getEntityManager();
        jakarta.persistence.EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Cập nhật trạng thái status = false (Trigger trg_BaoVeStoreOwnerDuyNhat sẽ tự động chặn nếu là STORE_OWNER active duy nhất)
            int updated = em.createNativeQuery(
                    "UPDATE dbo.employee SET status = 0 WHERE UPPER(employee_id) = UPPER(:id)"
            ).setParameter("id", employeeId.trim()).executeUpdate();
            tx.commit();
            return updated > 0;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            String msg = root.getMessage();
            if (msg != null && msg.contains("Không thể xóa hoặc vô hiệu hóa Quản trị viên")) {
                throw new com.mycompany.quanlysieuthi.common.exception.AppException.BusinessException(
                        "Không thể xóa hoặc vô hiệu hóa Quản trị viên (STORE_OWNER) duy nhất trong hệ thống!");
            }
            throw new RuntimeException("Lỗi vô hiệu hóa nhân viên: " + msg, e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Báo cáo hiệu suất thu ngân theo ngày từ View dbo.v_hieu_suat_thu_ngan_theo_ngay.
     */
    public List<EmployeeResponse.CashierPerformanceDto> findCashierShiftPerformance(java.time.LocalDate shiftDate) {
        EntityManager em = getEntityManager();
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT employee_id, cashier_name, shift_date, total_transactions, total_collected, cash_amount, online_amount " +
                    "FROM dbo.v_hieu_suat_thu_ngan_theo_ngay WHERE 1=1 "
            );
            if (shiftDate != null) {
                sql.append("AND shift_date = :shiftDate ");
            }
            sql.append("ORDER BY shift_date DESC, total_collected DESC");

            jakarta.persistence.Query q = em.createNativeQuery(sql.toString());
            if (shiftDate != null) {
                q.setParameter("shiftDate", java.sql.Date.valueOf(shiftDate));
            }

            List<?> rows = q.getResultList();
            List<EmployeeResponse.CashierPerformanceDto> list = new java.util.ArrayList<>();
            for (Object obj : rows) {
                Object[] r = (Object[]) obj;
                String empId = (String) r[0];
                String name = (String) r[1];
                String dateStr = r[2] != null ? r[2].toString() : "";
                long txCount = r[3] != null ? ((Number) r[3]).longValue() : 0L;
                java.math.BigDecimal total = r[4] != null ? new java.math.BigDecimal(r[4].toString()) : java.math.BigDecimal.ZERO;
                java.math.BigDecimal cash = r[5] != null ? new java.math.BigDecimal(r[5].toString()) : java.math.BigDecimal.ZERO;
                java.math.BigDecimal online = r[6] != null ? new java.math.BigDecimal(r[6].toString()) : java.math.BigDecimal.ZERO;

                list.add(new EmployeeResponse.CashierPerformanceDto(empId, name, dateStr, txCount, total, cash, online));
            }
            return list;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public record PageResult<T>(List<T> items, long totalElements) {
        public List<T> getItems() { return items; }
        public long getTotalElements() { return totalElements; }
    }

    public PageResult<Employee> findEmployees(int page, int limit, String keyword, String position, Boolean status) {
        EntityManager em = getEntityManager();
        try {
            StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
            java.util.Map<String, Object> params = new java.util.LinkedHashMap<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                whereClause.append(" AND (LOWER(e.fullName) LIKE :keyword OR LOWER(e.employeeId) LIKE :keyword OR LOWER(e.username) LIKE :keyword) ");
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
