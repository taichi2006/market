package com.mycompany.quanlysieuthi.modules.employee;

import com.mycompany.quanlysieuthi.common.exception.AppException.*;
import com.mycompany.quanlysieuthi.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test cho EmployeeService")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository);
    }

    @Nested
    @DisplayName("Kiểm thử getCashierPerformance() với View v_hieu_suat_thu_ngan_theo_ngay")
    class GetCashierPerformanceTests {

        @Test
        @DisplayName("Lấy báo cáo hiệu suất thu ngân theo ngày thành công")
        void testGetCashierPerformance_Success() {
            LocalDate shiftDate = LocalDate.parse("2026-10-15");
            EmployeeResponse.CashierPerformanceDto dto = new EmployeeResponse.CashierPerformanceDto(
                    "NV000002", "Trần Thu Ngân", "2026-10-15",
                    25L, new BigDecimal("12500000.00"), new BigDecimal("5000000.00"), new BigDecimal("7500000.00")
            );

            when(employeeRepository.findCashierShiftPerformance(shiftDate))
                    .thenReturn(List.of(dto));

            List<EmployeeResponse.CashierPerformanceDto> result = employeeService.getCashierPerformance("2026-10-15");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("NV000002", result.get(0).employeeId());
            assertEquals(25L, result.get(0).totalTransactions());
            assertEquals(new BigDecimal("12500000.00"), result.get(0).totalCollected());
            verify(employeeRepository).findCashierShiftPerformance(shiftDate);
        }

        @Test
        @DisplayName("Ném BadRequestException khi dateStr không đúng định dạng YYYY-MM-DD")
        void testGetCashierPerformance_InvalidDateFormat() {
            assertThrows(BadRequestException.class, () ->
                    employeeService.getCashierPerformance("15/10/2026"));
        }
    }

    @Nested
    @DisplayName("Kiểm thử deleteEmployee() với bảo vệ từ Trigger trg_BaoVeStoreOwnerDuyNhat")
    class DeleteEmployeeTests {

        @Test
        @DisplayName("Vô hiệu hóa nhân viên thành công")
        void testDeleteEmployee_Success() {
            Employee emp = new Employee();
            emp.setEmployeeId("NV000002");
            emp.setUsername("cashier1");
            emp.setStatus(true);
            emp.setPosition("CASHIER");

            when(employeeRepository.findByIdIgnoreCase("NV000002")).thenReturn(Optional.of(emp));
            when(employeeRepository.softDelete("NV000002")).thenReturn(true);

            EmployeeResponse.DeleteResult result = employeeService.deleteEmployee("NV000002", "admin");

            assertNotNull(result);
            assertEquals("NV000002", result.employeeId());
            assertEquals(Boolean.FALSE, result.status());
            verify(employeeRepository).softDelete("NV000002");
        }

        @Test
        @DisplayName("Chặn tự vô hiệu hóa tài khoản của chính mình")
        void testDeleteEmployee_SelfDeleteBlocked() {
            Employee emp = new Employee();
            emp.setEmployeeId("NV000001");
            emp.setUsername("admin");
            emp.setStatus(true);

            when(employeeRepository.findByIdIgnoreCase("NV000001")).thenReturn(Optional.of(emp));

            assertThrows(BadRequestException.class, () ->
                    employeeService.deleteEmployee("NV000001", "NV000001"));
            assertThrows(BadRequestException.class, () ->
                    employeeService.deleteEmployee("NV000001", "admin"));
        }

        @Test
        @DisplayName("Ném BusinessException khi Trigger trg_BaoVeStoreOwnerDuyNhat chặn xóa Quản trị viên duy nhất")
        void testDeleteEmployee_StoreOwnerProtectionTrigger() {
            Employee emp = new Employee();
            emp.setEmployeeId("NV000001");
            emp.setUsername("owner1");
            emp.setStatus(true);
            emp.setPosition("STORE_OWNER");

            when(employeeRepository.findByIdIgnoreCase("NV000001")).thenReturn(Optional.of(emp));
            when(employeeRepository.softDelete("NV000001"))
                    .thenThrow(new BusinessException("Lỗi bảo mật: Không thể xóa hoặc vô hiệu hóa Quản trị viên (STORE_OWNER) duy nhất trong hệ thống!"));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    employeeService.deleteEmployee("NV000001", "otherUser"));

            assertTrue(ex.getMessage().contains("STORE_OWNER"));
            verify(employeeRepository).softDelete("NV000001");
        }
    }
}
