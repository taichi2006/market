package com.mycompany.quanlysieuthi.modules.auth;

import com.mycompany.quanlysieuthi.common.base.BaseRepository;
import com.mycompany.quanlysieuthi.entity.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.Set;

/**
 * Repository handling database operations for Authentication.
 * Đồng bộ với các tính năng CSDL trong auth_receipt_fb.sql:
 * - Ràng buộc CHECK chk_employee_position: position IN ('STORE_OWNER', 'CASHIER', 'INVENTORY_MANAGER')
 * - Ánh xạ người dùng & vai trò CSDL (Database Roles): role_owner, role_cashier, role_inventory, role_customer
 * - Phân quyền bảo mật (GRANT / DENY) tại tầng DBMS.
 */
public class AuthRepository extends BaseRepository<Employee, String> {

    public static final Set<String> VALID_POSITIONS = Set.of("STORE_OWNER", "CASHIER", "INVENTORY_MANAGER");

    public AuthRepository() {
        super(Employee.class);
    }

    /**
     * Ánh xạ chức vụ nhân sự sang Database Role tương ứng trong SQL Server.
     */
    public String getDatabaseRoleForPosition(String position) {
        if (position == null) return null;
        return switch (position.trim().toUpperCase()) {
            case "STORE_OWNER" -> "role_owner";
            case "CASHIER" -> "role_cashier";
            case "INVENTORY_MANAGER" -> "role_inventory";
            default -> null;
        };
    }

    /**
     * Kiểm tra tính hợp lệ của chức vụ nhân viên dựa trên ràng buộc chk_employee_position.
     */
    public boolean isValidPosition(String position) {
        return position != null && VALID_POSITIONS.contains(position.trim().toUpperCase());
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

