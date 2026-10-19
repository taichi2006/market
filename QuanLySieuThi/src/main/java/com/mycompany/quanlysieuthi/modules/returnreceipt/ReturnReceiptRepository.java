package com.mycompany.quanlysieuthi.modules.returnreceipt;

import com.mycompany.quanlysieuthi.common.base.BaseRepository;
import com.mycompany.quanlysieuthi.common.exception.AppException.*;
import com.mycompany.quanlysieuthi.config.JpaUtil;
import com.mycompany.quanlysieuthi.entity.Customer;
import com.mycompany.quanlysieuthi.entity.Invoice;
import com.mycompany.quanlysieuthi.entity.Product;
import com.mycompany.quanlysieuthi.entity.ReturnReceipt;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository handling database operations for Return Receipt module.
 * Tận dụng các tính năng CSDL nâng cao:
 * - Stored Procedure: dbo.sp_LapPhieuTraHang (kèm Giao dịch Transaction)
 * - Scalar Function: dbo.fn_TongTienHoanTraTheoHoaDon
 * - View: dbo.v_danh_sach_phieu_tra_hang
 * - Trigger: dbo.trg_CapNhatTrangThaiHoaDon_DoiTra (tự động cập nhật trạng thái hóa đơn sang REFUNDED)
 * - Ràng buộc toàn vẹn: chk_return_valid_data, uq_return_receipt_invoice
 */
public class ReturnReceiptRepository extends BaseRepository<ReturnReceipt, String> {

    public ReturnReceiptRepository() {
        super(ReturnReceipt.class);
    }

    public Optional<Invoice> findInvoiceById(String invoiceId) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Invoice invoice = em.find(Invoice.class, invoiceId.trim());
            return Optional.ofNullable(invoice);
        } finally {
            em.close();
        }
    }

    public Optional<Customer> findCustomerById(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Customer customer = em.find(Customer.class, customerId.trim());
            return Optional.ofNullable(customer);
        } finally {
            em.close();
        }
    }

    public Optional<Product> findProductById(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Product product = em.find(Product.class, productId.trim());
            return Optional.ofNullable(product);
        } finally {
            em.close();
        }
    }

    public List<Object[]> findInvoiceItems(String invoiceId) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        EntityManager em = JpaUtil.getEntityManager();
        try {
            String sql = "SELECT product_id, quantity, unit_price FROM invoice_detail WHERE invoice_id = :invoiceId";
            TypedQuery<Object[]> query = em.unwrap(Session.class)
                    .createNativeQuery(sql, Object[].class);
            query.setParameter("invoiceId", invoiceId.trim());

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Gọi Scalar Function dbo.fn_TongTienHoanTraTheoHoaDon để tính tổng tiền đã hoàn trả cho một hóa đơn.
     */
    public BigDecimal calculateTotalRefundByInvoice(String invoiceId) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String sql = "SELECT dbo.fn_TongTienHoanTraTheoHoaDon(:invoiceId)";
            Object result = em.createNativeQuery(sql)
                    .setParameter("invoiceId", invoiceId.trim())
                    .getSingleResult();
            if (result instanceof BigDecimal bd) {
                return bd;
            } else if (result instanceof Number num) {
                return BigDecimal.valueOf(num.doubleValue());
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra hóa đơn đã lập phiếu đổi trả hay chưa dựa trên View dbo.v_danh_sach_phieu_tra_hang
     * (thay thế truy vấn bảng trực tiếp, đồng thời CSDL có ràng buộc uq_return_receipt_invoice).
     */
    public boolean existsReturnReceiptByInvoiceId(String invoiceId) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String sql = "SELECT COUNT(1) FROM dbo.v_danh_sach_phieu_tra_hang WHERE original_invoice_id = :invoiceId";
            Number count = (Number) em.createNativeQuery(sql)
                    .setParameter("invoiceId", invoiceId.trim())
                    .getSingleResult();
            return count != null && count.longValue() > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Lập phiếu trả hàng thông qua Stored Procedure dbo.sp_LapPhieuTraHang:
     * - Stored Procedure tự động kiểm tra tồn tại hóa đơn gốc.
     * - Stored Procedure gọi fn_TongTienHoanTraTheoHoaDon để kiểm soát tài chính.
     * - Trigger trg_CapNhatTrangThaiHoaDon_DoiTra tự động chuyển trạng thái hóa đơn sang REFUNDED.
     * - Ràng buộc UNIQUE uq_return_receipt_invoice và CHECK chk_return_valid_data bảo đảm toàn vẹn dữ liệu.
     */
    public void saveReturnReceiptTransaction(ReturnReceipt receipt,
                                             Map<String, Integer> productRestockMap,
                                             String customerId,
                                             int pointsDeducted) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // 1. Gọi Stored Procedure dbo.sp_LapPhieuTraHang thay cho lệnh INSERT JPA thông thường
            em.createNativeQuery(
                    "EXEC dbo.sp_LapPhieuTraHang :returnReceiptId, :invoiceId, :employeeId, :refundAmount, :reason")
                    .setParameter("returnReceiptId", receipt.getReturnReceiptId())
                    .setParameter("invoiceId", receipt.getInvoiceId())
                    .setParameter("employeeId", receipt.getEmployeeId() != null ? receipt.getEmployeeId() : "")
                    .setParameter("refundAmount", receipt.getRefundAmount())
                    .setParameter("reason", receipt.getReason() != null ? receipt.getReason() : "")
                    .executeUpdate();

            // 2. Hoàn lại số lượng tồn kho cho các sản phẩm trả lại
            if (productRestockMap != null) {
                for (Map.Entry<String, Integer> entry : productRestockMap.entrySet()) {
                    String productId = entry.getKey();
                    int restockQty = entry.getValue();

                    Product product = em.find(Product.class, productId);
                    if (product != null) {
                        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                        product.setStockQuantity(currentStock + restockQty);
                        em.merge(product);
                    }
                }
            }

            // 3. Khấu trừ điểm tích lũy của khách hàng thành viên tương ứng với số tiền hoàn trả
            if (customerId != null && !customerId.trim().isEmpty() && pointsDeducted > 0) {
                Customer customer = em.find(Customer.class, customerId.trim());
                if (customer != null) {
                    int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
                    customer.setLoyaltyPoints(Math.max(0, currentPoints - pointsDeducted));
                    em.merge(customer);
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            String msg = root.getMessage();
            if (msg != null && (msg.contains("uq_return_receipt_invoice") || msg.contains("Violation of UNIQUE KEY constraint"))) {
                throw new ConflictException("Hóa đơn này đã được lập phiếu trả hàng trước đó");
            }
            if (msg != null && msg.contains("chk_return_valid_data")) {
                throw new IllegalArgumentException("Dữ liệu không hợp lệ: Tiền hoàn trả phải > 0 và lý do đổi trả phải có ít nhất 5 ký tự.");
            }
            if (msg != null && msg.contains("Giao dịch thất bại: Hóa đơn gốc không tồn tại")) {
                throw new NotFoundException("Hóa đơn không tồn tại trong hệ thống: " + receipt.getInvoiceId());
            }
            if (msg != null && msg.contains("Tổng số tiền hoàn trả vượt quá")) {
                throw new IllegalArgumentException(msg);
            }
            throw new RuntimeException("Lỗi giao dịch khi lưu phiếu trả hàng: " + (msg != null ? msg : e.getMessage()), e);
        } finally {
            em.close();
        }
    }

    /**
     * Lấy danh sách phiếu trả hàng thông qua Khung nhìn (View) dbo.v_danh_sach_phieu_tra_hang.
     */
    public List<Object[]> findReturnReceipts(int page, int limit, String invoiceId, String employeeId,
                                             LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT v.return_receipt_id, v.original_invoice_id, v.refund_amount, v.return_reason, v.requested_at, " +
                    "e.employee_id, e.full_name " +
                    "FROM dbo.v_danh_sach_phieu_tra_hang v " +
                    "LEFT JOIN dbo.employee e ON v.employee_id = e.employee_id " +
                    "WHERE 1=1 ");

            if (invoiceId != null && !invoiceId.trim().isEmpty()) {
                sql.append("AND v.original_invoice_id = :invoiceId ");
            }
            if (employeeId != null && !employeeId.trim().isEmpty()) {
                sql.append("AND v.employee_id = :employeeId ");
            }
            if (fromDateTime != null) {
                sql.append("AND v.requested_at >= :fromDateTime ");
            }
            if (toDateTime != null) {
                sql.append("AND v.requested_at <= :toDateTime ");
            }

            sql.append("ORDER BY v.requested_at DESC");

            Query query = em.createNativeQuery(sql.toString());

            if (invoiceId != null && !invoiceId.trim().isEmpty()) {
                query.setParameter("invoiceId", invoiceId.trim());
            }
            if (employeeId != null && !employeeId.trim().isEmpty()) {
                query.setParameter("employeeId", employeeId.trim());
            }
            if (fromDateTime != null) {
                query.setParameter("fromDateTime", fromDateTime);
            }
            if (toDateTime != null) {
                query.setParameter("toDateTime", toDateTime);
            }

            int offset = (page - 1) * limit;
            query.setFirstResult(offset);
            query.setMaxResults(limit);

            @SuppressWarnings("unchecked")
            List<Object[]> rawList = query.getResultList();
            List<Object[]> result = new ArrayList<>(rawList.size());
            for (Object[] row : rawList) {
                LocalDateTime createdAt = null;
                if (row[4] instanceof Timestamp ts) {
                    createdAt = ts.toLocalDateTime();
                } else if (row[4] instanceof LocalDateTime ldt) {
                    createdAt = ldt;
                }
                BigDecimal refundAmt = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
                result.add(new Object[]{
                        row[0],
                        row[1],
                        refundAmt,
                        row[3],
                        createdAt,
                        row[5],
                        row[6]
                });
            }
            return result;
        } finally {
            em.close();
        }
    }

    /**
     * Lấy chi tiết phiếu trả hàng thông qua Khung nhìn (View) dbo.v_danh_sach_phieu_tra_hang kết hợp các bảng liên quan.
     */
    public Optional<Object[]> findReturnReceiptHeaderById(String returnReceiptId) {
        if (returnReceiptId == null || returnReceiptId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String sql = "SELECT v.return_receipt_id, v.original_invoice_id, v.refund_amount, v.return_reason, v.requested_at, " +
                    "e.employee_id, e.full_name, e.username, " +
                    "c.customer_id, c.full_name AS customer_name, c.phone_number " +
                    "FROM dbo.v_danh_sach_phieu_tra_hang v " +
                    "LEFT JOIN dbo.employee e ON v.employee_id = e.employee_id " +
                    "LEFT JOIN dbo.invoice i ON v.original_invoice_id = i.invoice_id " +
                    "LEFT JOIN dbo.customer c ON i.customer_id = c.customer_id " +
                    "WHERE v.return_receipt_id = :returnReceiptId";

            Query query = em.createNativeQuery(sql);
            query.setParameter("returnReceiptId", returnReceiptId.trim());

            @SuppressWarnings("unchecked")
            List<Object[]> list = query.getResultList();
            if (list.isEmpty()) {
                return Optional.empty();
            }
            Object[] row = list.get(0);
            LocalDateTime createdAt = null;
            if (row[4] instanceof Timestamp ts) {
                createdAt = ts.toLocalDateTime();
            } else if (row[4] instanceof LocalDateTime ldt) {
                createdAt = ldt;
            }
            BigDecimal refundAmt = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
            return Optional.of(new Object[]{
                    row[0],
                    row[1],
                    refundAmt,
                    row[3],
                    createdAt,
                    row[5],
                    row[6],
                    row[7],
                    row[8],
                    row[9],
                    row[10]
            });
        } finally {
            em.close();
        }
    }
}

