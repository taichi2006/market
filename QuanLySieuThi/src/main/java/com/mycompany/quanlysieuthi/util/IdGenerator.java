package com.mycompany.quanlysieuthi.util;

import com.mycompany.quanlysieuthi.config.JpaUtil;
import com.mycompany.quanlysieuthi.entity.Customer;
import com.mycompany.quanlysieuthi.entity.Employee;
import com.mycompany.quanlysieuthi.entity.Feedback;
import com.mycompany.quanlysieuthi.entity.InventoryReport;
import com.mycompany.quanlysieuthi.entity.Invoice;
import com.mycompany.quanlysieuthi.entity.InvoiceDetail;
import com.mycompany.quanlysieuthi.entity.Payment;
import com.mycompany.quanlysieuthi.entity.Product;
import com.mycompany.quanlysieuthi.entity.ProductCategory;
import com.mycompany.quanlysieuthi.entity.PurchaseReceiptDetail;
import com.mycompany.quanlysieuthi.entity.ReturnReceipt;
import com.mycompany.quanlysieuthi.entity.Voucher;
import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility tu dong tao ma dinh danh chuan theo tien to (Prefix) va so thu tu tang dan.
 * Dinh dang: [TIEN_TO][001, 002, 003...]
 *
 * Vi du:
 * - San pham: SP001, SP002...
 * - Nhan vien: NV001, NV002...
 * - Khach hang: KH001, KH002...
 * - Hoa don: HD001, HD002...
 * - Chi tiet hoa don: CTHD001, CTHD002...
 * - Phieu nhap (Voucher): PN001, PN002...
 * - Chi tiet phieu nhap: CTPN001, CTPN002...
 * - Phieu tra hang: PT001, PT002...
 * - Bien ban kiem ke kho: KK001, KK002...
 * - Giao dich thanh toan: GD001, GD002...
 * - Danh gia / Phan hoi: DG001, DG002...
 */
public final class IdGenerator {

    private IdGenerator() {
    }

    /**
     * Sinh ma tiep theo cho entity dua tren tien to (prefix).
     */
    public static synchronized String generateId(Class<?> entityClass, String idFieldName, String prefix) {
        return generateNextIds(entityClass, idFieldName, prefix, 1).get(0);
    }

    /**
     * Sinh danh sach gom `count` ma tiep theo lien tiep (dung cho batch insert nhu items trong phieu nhap/hoa don).
     */
    public static synchronized List<String> generateNextIds(Class<?> entityClass, String idFieldName, String prefix, int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }

        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Tối ưu trực tiếp từ SQL: Sắp xếp theo độ dài và thứ tự giảm dần, chỉ lấy đúng 1 bản ghi lớn nhất từ Database
            String jpql = "SELECT e." + idFieldName + " FROM " + entityClass.getSimpleName() + " e " +
                          "WHERE e." + idFieldName + " LIKE :pattern " +
                          "ORDER BY LENGTH(e." + idFieldName + ") DESC, e." + idFieldName + " DESC";

            List<String> maxIdList = em.createQuery(jpql, String.class)
                    .setParameter("pattern", prefix + "%")
                    .setMaxResults(1)
                    .getResultList();

            long maxNumber = 0;
            if (!maxIdList.isEmpty() && maxIdList.get(0) != null) {
                String latestId = maxIdList.get(0).trim();
                if (latestId.startsWith(prefix)) {
                    String numPart = latestId.substring(prefix.length()).trim();
                    try {
                        maxNumber = Long.parseLong(numPart);
                    } catch (NumberFormatException ignored) {
                        // Bỏ qua nếu phần sau tiền tố không phải số
                    }
                }
            }

            List<String> result = new ArrayList<>(count);
            for (int i = 1; i <= count; i++) {
                long nextNum = maxNumber + i;
                result.add(String.format("%s%03d", prefix, nextNum));
            }
            return result;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    // --- Cac ham tien ich truc tiep cho tung module ---

    /** San pham: SP001, SP002... */
    public static String nextProductId() {
        return generateId(Product.class, "productId", "SP");
    }

    public static List<String> nextProductIds(int count) {
        return generateNextIds(Product.class, "productId", "SP", count);
    }

    /** Nhan vien: NV001, NV002... */
    public static String nextEmployeeId() {
        return generateId(Employee.class, "employeeId", "NV");
    }

    /** Khach hang: KH001, KH002... */
    public static String nextCustomerId() {
        return generateId(Customer.class, "customerId", "KH");
    }

    /** Hoa don: HD001, HD002... */
    public static String nextInvoiceId() {
        return generateId(Invoice.class, "invoiceId", "HD");
    }

    /** Chi tiet hoa don: CTHD001, CTHD002... */
    public static String nextInvoiceDetailId() {
        return generateId(InvoiceDetail.class, "invoiceDetailId", "CTHD");
    }

    public static List<String> nextInvoiceDetailIds(int count) {
        return generateNextIds(InvoiceDetail.class, "invoiceDetailId", "CTHD", count);
    }

    /** Phieu nhap hang (Voucher): PN001, PN002... */
    public static String nextVoucherId() {
        return generateId(Voucher.class, "voucherId", "PN");
    }

    /** Chi tiet phieu nhap: CTPN001, CTPN002... */
    public static String nextPurchaseDetailId() {
        return generateId(PurchaseReceiptDetail.class, "purchaseDetailId", "CTPN");
    }

    public static List<String> nextPurchaseDetailIds(int count) {
        return generateNextIds(PurchaseReceiptDetail.class, "purchaseDetailId", "CTPN", count);
    }

    /** Phieu tra hang: PT001, PT002... */
    public static String nextReturnReceiptId() {
        return generateId(ReturnReceipt.class, "returnReceiptId", "PT");
    }

    /** Bien ban kiem ke kho: KK001, KK002... */
    public static String nextReportId() {
        return generateId(InventoryReport.class, "reportId", "KK");
    }

    /** Giao dich thanh toan: GD001, GD002... */
    public static String nextTransactionId() {
        return generateId(Payment.class, "transactionId", "GD");
    }

    /** Danh gia phan hoi khach hang: DG001, DG002... */
    public static String nextFeedbackId() {
        return generateId(Feedback.class, "feedbackId", "DG");
    }

    /** Loai san pham: LSP001, LSP002... */
    public static String nextCategoryId() {
        return generateId(ProductCategory.class, "categoryId", "LSP");
    }

    /** Phieu xuat huy kho: PH001, PH002... */
    public static String nextDisposalVoucherId() {
        return generateId(Voucher.class, "voucherId", "PH");
    }

    /** Chi tiet phieu huy: CTPH001, CTPH002... */
    public static String nextDisposalDetailId() {
        return generateId(com.mycompany.quanlysieuthi.entity.DisposalReceiptDetail.class, "disposalDetailId", "CTPH");
    }

    public static List<String> nextDisposalDetailIds(int count) {
        return generateNextIds(com.mycompany.quanlysieuthi.entity.DisposalReceiptDetail.class, "disposalDetailId", "CTPH", count);
    }
}
