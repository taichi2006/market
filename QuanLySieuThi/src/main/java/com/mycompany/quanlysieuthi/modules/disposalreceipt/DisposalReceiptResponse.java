package com.mycompany.quanlysieuthi.modules.disposalreceipt;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO definitions for Disposal Receipt module.
 */
public class DisposalReceiptResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    public record EmployeeDto(String employeeId, String fullName, String username) implements Serializable {
        public EmployeeDto(String employeeId, String fullName) {
            this(employeeId, fullName, null);
        }
    }

    public record CreateResponse(
            String voucherId,
            BigDecimal totalAmount,
            String createdAt,
            EmployeeDto createdEmployee,
            int itemsCount
    ) implements Serializable {}

    public record ListItemDto(
            String voucherId,
            BigDecimal totalAmount,
            String createdAt,
            EmployeeDto employee,
            int itemsCount
    ) implements Serializable {}

    public record DetailItemDto(
            String disposalDetailId,
            String productId,
            String productName,
            String categoryName,
            BigDecimal unitPrice,
            Integer disposedQuantity,
            String disposalReason,
            String expirationDate
    ) implements Serializable {}

    public record DetailResponse(
            String voucherId,
            BigDecimal totalAmount,
            String createdAt,
            EmployeeDto employee,
            int itemsCount,
            List<DetailItemDto> items
    ) implements Serializable {}
}
