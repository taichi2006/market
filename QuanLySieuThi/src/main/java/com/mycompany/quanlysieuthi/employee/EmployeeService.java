package com.mycompany.quanlysieuthi.employee;

import com.mycompany.quanlysieuthi.position.Position;
import com.mycompany.quanlysieuthi.position.PositionDao;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service handling business logic and bidirectional mapping for Employee.
 */
public class EmployeeService {

    private final EmployeeDao employeeDao;
    private final PositionDao positionDao;

    public EmployeeService() {
        this.employeeDao = new EmployeeDao();
        this.positionDao = new PositionDao();
    }

    public EmployeeService(EmployeeDao employeeDao, PositionDao positionDao) {
        this.employeeDao = employeeDao;
        this.positionDao = positionDao;
    }

    public List<EmployeeResponse> getAllEmployees() {
        return employeeDao.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EmployeeResponse getEmployeeById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return employeeDao.findById(id.trim())
                .map(this::toResponse)
                .orElse(null);
    }

    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Employee request payload cannot be null");
        }
        if (request.getPositionId() == null || request.getPositionId().trim().isEmpty()) {
            throw new IllegalArgumentException("positionId is required");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("fullName is required");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("username is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("password is required");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("phone is required");
        }

        Position position = positionDao.findById(request.getPositionId().trim())
                .orElseThrow(() -> new IllegalArgumentException("Position not found with ID: " + request.getPositionId()));

        Employee employee = toEntity(request, position);
        Employee savedEmployee = employeeDao.save(employee);
        return toResponse(savedEmployee);
    }

    public EmployeeResponse updateEmployee(String id, EmployeeRequest request) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("Update payload cannot be null");
        }

        Employee employee = employeeDao.findById(id.trim())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));

        if (request.getPositionId() != null && !request.getPositionId().trim().isEmpty()) {
            Position position = positionDao.findById(request.getPositionId().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Position not found with ID: " + request.getPositionId()));
            employee.setPosition(position);
        }
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            employee.setFullName(request.getFullName().trim());
        }
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            employee.setUsername(request.getUsername().trim());
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            employee.setPassword(request.getPassword());
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            employee.setPhone(request.getPhone().trim());
        }
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }

        Employee saved = employeeDao.save(employee);
        return toResponse(saved);
    }

    public EmployeeResponse deactivateEmployee(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID is required");
        }

        Employee employee = employeeDao.findById(id.trim())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));

        employee.setStatus(false);
        Employee saved = employeeDao.save(employee);
        return toResponse(saved);
    }

    public Employee toEntity(EmployeeRequest request, Position position) {
        if (request == null) {
            return null;
        }

        String employeeId = (request.getId() != null && !request.getId().trim().isEmpty())
                ? request.getId().trim()
                : UUID.randomUUID().toString();

        Boolean status = (request.getStatus() != null) ? request.getStatus() : Boolean.TRUE;

        return new Employee(
                employeeId,
                position,
                request.getFullName().trim(),
                request.getUsername().trim(),
                request.getPassword(),
                request.getPhone().trim(),
                status
        );
    }

    public EmployeeResponse toResponse(Employee entity) {
        if (entity == null) {
            return null;
        }
        String positionId = null;
        String positionName = null;
        if (entity.getPosition() != null) {
            positionId = entity.getPosition().getId();
            try {
                positionName = entity.getPosition().getName();
            } catch (Exception e) {
                // Safe fallback in case proxy is detached
                Position pos = positionDao.findById(positionId).orElse(null);
                if (pos != null) {
                    positionName = pos.getName();
                }
            }
        }

        return new EmployeeResponse(
                entity.getId(),
                positionId,
                positionName,
                entity.getFullName(),
                entity.getUsername(),
                entity.getPhone(),
                entity.getStatus()
        );
    }
}
