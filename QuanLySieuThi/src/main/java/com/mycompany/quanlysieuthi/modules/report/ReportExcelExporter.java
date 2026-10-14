package com.mycompany.quanlysieuthi.modules.report;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility handling Apache POI Excel workbook creation and styling for reports.
 */
public class ReportExcelExporter {

    private final RevenueRepository revenueRepository;

    public ReportExcelExporter(RevenueRepository revenueRepository) {
        this.revenueRepository = revenueRepository;
    }

    public String exportExcel(String target, String receiptId, ReportService.DateRange dr, OutputStream out) throws IOException {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter dateOnlyDf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        boolean isDiscrepancy = target != null && (
                target.equalsIgnoreCase("discrepancy") ||
                target.equalsIgnoreCase("huy") ||
                target.equalsIgnoreCase("kiem-ke") ||
                target.equalsIgnoreCase("disposal") ||
                target.equalsIgnoreCase("stock-discrepancy")
        );

        Workbook workbook = new XSSFWorkbook();
        try {
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.cloneStyleFrom(dataStyle);
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.cloneStyleFrom(dataStyle);
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);
            DataFormat format = workbook.createDataFormat();
            numberStyle.setDataFormat(format.getFormat("#,##0"));

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.cloneStyleFrom(dataStyle);
            currencyStyle.setAlignment(HorizontalAlignment.RIGHT);
            currencyStyle.setDataFormat(format.getFormat("#,##0 \"đ\""));

            CellStyle boldNumberStyle = workbook.createCellStyle();
            boldNumberStyle.cloneStyleFrom(numberStyle);
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldNumberStyle.setFont(boldFont);

            CellStyle boldCurrencyStyle = workbook.createCellStyle();
            boldCurrencyStyle.cloneStyleFrom(currencyStyle);
            boldCurrencyStyle.setFont(boldFont);

            CellStyle totalLabelStyle = workbook.createCellStyle();
            totalLabelStyle.cloneStyleFrom(dataStyle);
            totalLabelStyle.setFont(boldFont);
            totalLabelStyle.setAlignment(HorizontalAlignment.RIGHT);

            String filename;

            if (!isDiscrepancy) {
                filename = "Bao_Cao_Phieu_Nhap_" + dr.type + ".xlsx";
                Sheet sheet = workbook.createSheet("Phieu Nhap Kho");

                Row titleRow = sheet.createRow(0);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("BÁO CÁO PHIẾU NHẬP KHO");
                titleCell.setCellStyle(titleStyle);

                Row subRow = sheet.createRow(1);
                Cell subCell = subRow.createCell(0);
                subCell.setCellValue("Thời gian: Từ " + dr.from.format(dateOnlyDf) + " đến " + dr.to.format(dateOnlyDf)
                        + (receiptId != null && !receiptId.trim().isEmpty() ? " (Mã phiếu: " + receiptId.trim() + ")" : ""));

                String[] headers = {
                        "STT", "Mã phiếu", "Ngày nhập", "Nhà cung cấp", "Người nhập",
                        "Mã SP", "Tên sản phẩm", "Ngành hàng", "Hạn sử dụng",
                        "Số lượng nhập", "Đơn giá nhập", "Thành tiền"
                };

                Row headerRow = sheet.createRow(3);
                headerRow.setHeightInPoints(25);
                for (int i = 0; i < headers.length; i++) {
                    Cell c = headerRow.createCell(i);
                    c.setCellValue(headers[i]);
                    c.setCellStyle(headerStyle);
                }

                List<Object[]> rows = revenueRepository.findPurchaseReceiptRowsForExcel(receiptId, dr.from, dr.to);
                int rowIdx = 4;
                int stt = 1;
                long totalQty = 0;
                BigDecimal totalAmount = BigDecimal.ZERO;

                for (Object[] r : rows) {
                    Row row = sheet.createRow(rowIdx++);
                    String vId = (String) r[0];
                    LocalDateTime cAt = (LocalDateTime) r[1];
                    String supp = (String) r[2];
                    String emp = (String) r[3];
                    String pId = (String) r[4];
                    String pName = (String) r[5];
                    String catName = (String) r[6];
                    LocalDateTime exp = (LocalDateTime) r[7];
                    Integer qty = (Integer) r[8];
                    BigDecimal price = (BigDecimal) r[9];
                    BigDecimal lineTot = (BigDecimal) r[10];

                    int q = qty != null ? qty : 0;
                    totalQty += q;
                    if (lineTot != null) totalAmount = totalAmount.add(lineTot);

                    Cell c0 = row.createCell(0); c0.setCellValue(stt++); c0.setCellStyle(centerStyle);
                    Cell c1 = row.createCell(1); c1.setCellValue(vId != null ? vId : ""); c1.setCellStyle(centerStyle);
                    Cell c2 = row.createCell(2); c2.setCellValue(cAt != null ? cAt.format(df) : ""); c2.setCellStyle(centerStyle);
                    Cell c3 = row.createCell(3); c3.setCellValue(supp != null ? supp : ""); c3.setCellStyle(dataStyle);
                    Cell c4 = row.createCell(4); c4.setCellValue(emp != null ? emp : ""); c4.setCellStyle(dataStyle);
                    Cell c5 = row.createCell(5); c5.setCellValue(pId != null ? pId : ""); c5.setCellStyle(centerStyle);
                    Cell c6 = row.createCell(6); c6.setCellValue(pName != null ? pName : ""); c6.setCellStyle(dataStyle);
                    Cell c7 = row.createCell(7); c7.setCellValue(catName != null ? catName : ""); c7.setCellStyle(dataStyle);
                    Cell c8 = row.createCell(8); c8.setCellValue(exp != null ? exp.format(dateOnlyDf) : ""); c8.setCellStyle(centerStyle);
                    Cell c9 = row.createCell(9); c9.setCellValue(q); c9.setCellStyle(numberStyle);
                    Cell c10 = row.createCell(10); c10.setCellValue(price != null ? price.doubleValue() : 0); c10.setCellStyle(currencyStyle);
                    Cell c11 = row.createCell(11); c11.setCellValue(lineTot != null ? lineTot.doubleValue() : 0); c11.setCellStyle(currencyStyle);
                }

                Row totRow = sheet.createRow(rowIdx);
                for (int i = 0; i < headers.length; i++) {
                    totRow.createCell(i).setCellStyle(dataStyle);
                }
                Cell totLabel = totRow.getCell(8);
                totLabel.setCellValue("TỔNG CỘNG:");
                totLabel.setCellStyle(totalLabelStyle);

                Cell totQtyCell = totRow.getCell(9);
                totQtyCell.setCellValue(totalQty);
                totQtyCell.setCellStyle(boldNumberStyle);

                Cell totAmtCell = totRow.getCell(11);
                totAmtCell.setCellValue(totalAmount.doubleValue());
                totAmtCell.setCellStyle(boldCurrencyStyle);

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            } else {
                filename = "Bao_Cao_Xuat_Huy_Kiem_Ke_" + dr.type + ".xlsx";
                Sheet sheet = workbook.createSheet("Kiem Ke - Xuat Huy");

                Row titleRow = sheet.createRow(0);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("BÁO CÁO PHIẾU XUẤT HỦY VÀ KIỂM KÊ KHO");
                titleCell.setCellStyle(titleStyle);

                Row subRow = sheet.createRow(1);
                Cell subCell = subRow.createCell(0);
                subCell.setCellValue("Thời gian: Từ " + dr.from.format(dateOnlyDf) + " đến " + dr.to.format(dateOnlyDf)
                        + (receiptId != null && !receiptId.trim().isEmpty() ? " (Mã biên bản: " + receiptId.trim() + ")" : ""));

                String[] headers = {
                        "STT", "Mã biên bản", "Ngày kiểm kê", "Người kiểm",
                        "Mã SP", "Tên sản phẩm", "Ngành hàng",
                        "Tồn hệ thống", "Tồn thực tế", "Chênh lệch / Hủy", "Phân loại",
                        "Đơn giá bán", "Giá trị chênh lệch"
                };

                Row headerRow = sheet.createRow(3);
                headerRow.setHeightInPoints(25);
                for (int i = 0; i < headers.length; i++) {
                    Cell c = headerRow.createCell(i);
                    c.setCellValue(headers[i]);
                    c.setCellStyle(headerStyle);
                }

                List<Object[]> rows = revenueRepository.findStockDiscrepancyRowsForExcel(receiptId, dr.from, dr.to);
                int rowIdx = 4;
                int stt = 1;
                long totalDiff = 0;
                BigDecimal totalDiffVal = BigDecimal.ZERO;

                for (Object[] r : rows) {
                    Row row = sheet.createRow(rowIdx++);
                    String rId = (String) r[0];
                    LocalDateTime rDate = (LocalDateTime) r[1];
                    String emp = (String) r[2];
                    String pId = (String) r[3];
                    String pName = (String) r[4];
                    String catName = (String) r[5];
                    Integer sysQty = (Integer) r[6];
                    Integer actQty = (Integer) r[7];
                    Integer diff = (Integer) r[8];
                    BigDecimal price = (BigDecimal) r[9];

                    int d = diff != null ? diff : 0;
                    totalDiff += d;
                    BigDecimal p = price != null ? price : BigDecimal.ZERO;
                    BigDecimal diffValue = p.multiply(BigDecimal.valueOf(d));
                    totalDiffVal = totalDiffVal.add(diffValue);

                    String statusText;
                    if (d < 0) {
                        statusText = "THIẾU HỤT / XUẤT HỦY";
                    } else if (d > 0) {
                        statusText = "THỪA KHO";
                    } else {
                        statusText = "KHỚP";
                    }

                    Cell c0 = row.createCell(0); c0.setCellValue(stt++); c0.setCellStyle(centerStyle);
                    Cell c1 = row.createCell(1); c1.setCellValue(rId != null ? rId : ""); c1.setCellStyle(centerStyle);
                    Cell c2 = row.createCell(2); c2.setCellValue(rDate != null ? rDate.format(df) : ""); c2.setCellStyle(centerStyle);
                    Cell c3 = row.createCell(3); c3.setCellValue(emp != null ? emp : ""); c3.setCellStyle(dataStyle);
                    Cell c4 = row.createCell(4); c4.setCellValue(pId != null ? pId : ""); c4.setCellStyle(centerStyle);
                    Cell c5 = row.createCell(5); c5.setCellValue(pName != null ? pName : ""); c5.setCellStyle(dataStyle);
                    Cell c6 = row.createCell(6); c6.setCellValue(catName != null ? catName : ""); c6.setCellStyle(dataStyle);
                    Cell c7 = row.createCell(7); c7.setCellValue(sysQty != null ? sysQty : 0); c7.setCellStyle(numberStyle);
                    Cell c8 = row.createCell(8); c8.setCellValue(actQty != null ? actQty : 0); c8.setCellStyle(numberStyle);
                    Cell c9 = row.createCell(9); c9.setCellValue(d); c9.setCellStyle(numberStyle);
                    Cell c10 = row.createCell(10); c10.setCellValue(statusText); c10.setCellStyle(centerStyle);
                    Cell c11 = row.createCell(11); c11.setCellValue(p.doubleValue()); c11.setCellStyle(currencyStyle);
                    Cell c12 = row.createCell(12); c12.setCellValue(diffValue.doubleValue()); c12.setCellStyle(currencyStyle);
                }

                Row totRow = sheet.createRow(rowIdx);
                for (int i = 0; i < headers.length; i++) {
                    totRow.createCell(i).setCellStyle(dataStyle);
                }
                Cell totLabel = totRow.getCell(8);
                totLabel.setCellValue("TỔNG CỘNG:");
                totLabel.setCellStyle(totalLabelStyle);

                Cell totDiffCell = totRow.getCell(9);
                totDiffCell.setCellValue(totalDiff);
                totDiffCell.setCellStyle(boldNumberStyle);

                Cell totAmtCell = totRow.getCell(12);
                totAmtCell.setCellValue(totalDiffVal.doubleValue());
                totAmtCell.setCellStyle(boldCurrencyStyle);

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(out);
            return filename;
        } finally {
            workbook.close();
        }
    }
}
