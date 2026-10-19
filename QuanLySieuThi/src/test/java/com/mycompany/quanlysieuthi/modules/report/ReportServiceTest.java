package com.mycompany.quanlysieuthi.modules.report;

import com.mycompany.quanlysieuthi.common.exception.AppException.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test cho ReportService")
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private RevenueRepository revenueRepository;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(reportRepository, revenueRepository);
    }

    @Nested
    @DisplayName("Kiểm thử getStockDiscrepancyDetail() thông qua View vw_InventoryAuditDiscrepancy")
    class DiscrepancyDetailTests {

        @Test
        @DisplayName("Lấy chi tiết biên bản kiểm kê thành công từ View")
        void testGetStockDiscrepancyDetail_Success() {
            ReportResponse.EmployeeDto empDto = new ReportResponse.EmployeeDto("EMP001", "Nguyễn Văn A", "nva");
            ReportResponse.DetailItemDto itemDto = new ReportResponse.DetailItemDto(
                    "REP001_PRD001", "PRD001", "Bánh Oreo", "Bánh kẹo",
                    10, 8, -2, "SHORTAGE", "Thiếu 2 sản phẩm so với tồn kho hệ thống"
            );
            ReportResponse.DetailResponse mockDetail = new ReportResponse.DetailResponse(
                    "REP001", "Kiểm kê định kỳ", "Ghi chú", "2026-10-19T10:00:00.000Z",
                    empDto, 1, List.of(itemDto)
            );

            when(reportRepository.findAuditReportDetailById("REP001"))
                    .thenReturn(Optional.of(mockDetail));

            ReportResponse.DetailResponse result = reportService.getStockDiscrepancyDetail("REP001");

            assertNotNull(result);
            assertEquals("REP001", result.reportId());
            assertEquals(1, result.totalDiscrepancyItems());
            assertEquals("SHORTAGE", result.items().get(0).status());
            verify(reportRepository).findAuditReportDetailById("REP001");
        }

        @Test
        @DisplayName("Ném NotFoundException khi không tìm thấy biên bản kiểm kê")
        void testGetStockDiscrepancyDetail_NotFound_ThrowsException() {
            when(reportRepository.findAuditReportDetailById("REP-999"))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    reportService.getStockDiscrepancyDetail("REP-999")
            );
        }
    }

    @Nested
    @DisplayName("Kiểm thử createAuditSessionByCategory() qua Stored Procedure sp_CreateInventoryAuditSession")
    class CreateAuditSessionTests {

        @Test
        @DisplayName("Khởi tạo đợt kiểm kê theo danh mục thành công")
        void testCreateAuditSessionByCategory_Success() {
            ReportResponse.EmployeeDto empDto = new ReportResponse.EmployeeDto("EMP001", "Thủ kho", "thukho");
            ReportResponse.DetailResponse mockDetail = new ReportResponse.DetailResponse(
                    "REP-NEW", "Kiểm kê danh mục LSP001", null, "2026-10-19T10:00:00.000Z",
                    empDto, 0, Collections.emptyList()
            );

            when(reportRepository.createInventoryAuditSession(anyString(), eq("EMP001"), eq("LSP001")))
                    .thenReturn(5);
            when(reportRepository.findAuditReportDetailById(anyString()))
                    .thenReturn(Optional.of(mockDetail));

            ReportResponse.DetailResponse result = reportService.createAuditSessionByCategory(
                    "LSP001", "EMP001", "Thủ kho", "Kiểm kê danh mục LSP001", null
            );

            assertNotNull(result);
            assertEquals("REP-NEW", result.reportId());
            verify(reportRepository).createInventoryAuditSession(anyString(), eq("EMP001"), eq("LSP001"));
        }

        @Test
        @DisplayName("Ném IllegalArgumentException khi categoryId rỗng")
        void testCreateAuditSessionByCategory_EmptyCategory_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () ->
                    reportService.createAuditSessionByCategory("", "EMP001", "Thủ kho", null, null)
            );
        }
    }

    @Nested
    @DisplayName("Kiểm thử finalizeAuditReport() qua Stored Procedure sp_FinalizeAuditAndAutoDispose")
    class FinalizeAuditTests {

        @Test
        @DisplayName("Chốt đợt kiểm kê và tự động xuất hủy hàng thành công")
        void testFinalizeAuditReport_Success() {
            ReportResponse.EmployeeDto empDto = new ReportResponse.EmployeeDto("EMP001", "Thủ kho", "thukho");
            ReportResponse.DetailResponse mockDetail = new ReportResponse.DetailResponse(
                    "REP001", "Kiểm kê định kỳ", null, "2026-10-19T10:00:00.000Z",
                    empDto, 0, Collections.emptyList()
            );

            doNothing().when(reportRepository).finalizeAuditAndAutoDispose(eq("REP001"), anyString(), eq("EMP001"));
            when(reportRepository.findAuditReportDetailById("REP001")).thenReturn(Optional.of(mockDetail));

            ReportResponse.DetailResponse result = reportService.finalizeAuditReport("REP001", "EMP001");

            assertNotNull(result);
            assertEquals("REP001", result.reportId());
            verify(reportRepository).finalizeAuditAndAutoDispose(eq("REP001"), anyString(), eq("EMP001"));
        }

        @Test
        @DisplayName("Ném IllegalArgumentException khi reportId rỗng")
        void testFinalizeAuditReport_EmptyReportId_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () ->
                    reportService.finalizeAuditReport("  ", "EMP001")
            );
        }
    }

    @Nested
    @DisplayName("Kiểm thử getCategoryInventoryReport() qua View vw_ProductCategoryInventorySummary")
    class CategoryInventoryReportTests {

        @Test
        @DisplayName("Lấy báo cáo kho theo danh mục thành công")
        void testGetCategoryInventoryReport_Success() {
            Map<String, Object> row = Map.of(
                    "categoryId", "LSP001",
                    "categoryName", "Đồ khô",
                    "totalProducts", 20,
                    "totalStockQuantity", 500,
                    "totalInventoryValue", new BigDecimal("10000000.00")
            );
            when(reportRepository.findCategoryInventoryReport()).thenReturn(List.of(row));

            List<Map<String, Object>> result = reportService.getCategoryInventoryReport();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("LSP001", result.get(0).get("categoryId"));
            verify(reportRepository).findCategoryInventoryReport();
        }
    }
}
