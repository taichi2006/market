package com.mycompany.quanlysieuthi.modules.disposalreceipt;

import com.mycompany.quanlysieuthi.common.base.BaseRepository;
import com.mycompany.quanlysieuthi.entity.DisposalReceipt;
import com.mycompany.quanlysieuthi.entity.DisposalReceiptDetail;
import com.mycompany.quanlysieuthi.entity.Product;
import com.mycompany.quanlysieuthi.entity.Voucher;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

/**
 * Repository xử lý dữ liệu cho module Phiếu hủy kho (Disposal Receipt),
 * tận dụng View và Stored Procedure tối ưu.
 */
public class DisposalReceiptRepository extends BaseRepository<DisposalReceipt, String> {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public DisposalReceiptRepository() {
        super(DisposalReceipt.class);
    }

    public Optional<Product> findProductById(String productId) {
        if (productId == null || productId.isBlank())
            return Optional.empty();
        return query(em -> Optional.ofNullable(em.find(Product.class, productId.trim())));
    }

    public void saveDisposalReceiptTransaction(Voucher voucher,
            DisposalReceipt disposalReceipt,
            List<DisposalReceiptDetail> details,
            Map<String, Integer> productDeductionMap) {
        try {
            executeInTransaction(em -> {
                for (DisposalReceiptDetail detail : details) {
                    em.createNativeQuery(
                            "EXEC dbo.sp_CreateDisposalReceiptItem :voucherId, :employeeId, :detailId, :productId, :disposedQuantity, :disposalReason")
                            .setParameter("voucherId", voucher.getVoucherId())
                            .setParameter("employeeId", voucher.getEmployeeId() != null ? voucher.getEmployeeId() : "")
                            .setParameter("detailId", detail.getDisposalDetailId())
                            .setParameter("productId", detail.getProductId())
                            .setParameter("disposedQuantity", detail.getDisposedQuantity())
                            .setParameter("disposalReason",
                                    detail.getDisposalReason() != null ? detail.getDisposalReason() : "")
                            .executeUpdate();
                }
            });
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null)
                root = root.getCause();
            throw new RuntimeException("Lỗi giao dịch khi lưu phiếu hủy kho: " + root.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<DisposalReceiptResponse.ListItemDto> findDisposalReceipts(int page, int limit,
            String employeeId,
            LocalDateTime fromDateTime,
            LocalDateTime toDateTime) {
        return query(em -> {
            StringBuilder sql = new StringBuilder(
                    "SELECT voucher_id, total_amount, created_at, "
                            + "employee_id, employee_name, employee_username, total_items "
                            + "FROM v_disposal_receipt_summary WHERE 1=1 ");
            Map<String, Object> params = new HashMap<>();

            if (employeeId != null && !employeeId.isBlank()) {
                sql.append("AND employee_id = :employeeId ");
                params.put("employeeId", employeeId.trim());
            }
            if (fromDateTime != null) {
                sql.append("AND created_at >= :fromDateTime ");
                params.put("fromDateTime", fromDateTime);
            }
            if (toDateTime != null) {
                sql.append("AND created_at <= :toDateTime ");
                params.put("toDateTime", toDateTime);
            }

            sql.append("ORDER BY created_at DESC");
            Query query = em.createNativeQuery(sql.toString());
            params.forEach(query::setParameter);
            query.setFirstResult((page - 1) * limit);
            query.setMaxResults(limit);

            List<Object[]> rows = query.getResultList();
            List<DisposalReceiptResponse.ListItemDto> result = new ArrayList<>(rows.size());
            for (Object[] r : rows) {
                BigDecimal total = r[1] != null ? new BigDecimal(r[1].toString()) : BigDecimal.ZERO;
                DisposalReceiptResponse.EmployeeDto emp = r[3] != null
                        ? new DisposalReceiptResponse.EmployeeDto((String) r[3], (String) r[4], (String) r[5])
                        : null;
                int count = r[6] != null ? ((Number) r[6]).intValue() : 0;
                result.add(new DisposalReceiptResponse.ListItemDto((String) r[0], total, formatIso(r[2]), emp, count));
            }
            return result;
        });
    }

    public Optional<DisposalReceiptResponse.DetailResponse> findDisposalReceiptDetailById(String voucherId) {
        if (voucherId == null || voucherId.isBlank())
            return Optional.empty();

        return query(em -> {
            List<Object[]> headerList = em.createQuery(
                    "SELECT dr.voucherId, v.totalAmount, v.createdAt, "
                            + "e.employeeId, e.fullName, e.username "
                            + "FROM DisposalReceipt dr JOIN Voucher v ON dr.voucherId = v.voucherId "
                            + "LEFT JOIN Employee e ON v.employeeId = e.employeeId WHERE dr.voucherId = :voucherId",
                    Object[].class)
                    .setParameter("voucherId", voucherId.trim())
                    .getResultList();
            if (headerList.isEmpty())
                return Optional.empty();

            Object[] h = headerList.get(0);
            BigDecimal totalAmount = h[1] != null ? (BigDecimal) h[1] : BigDecimal.ZERO;
            DisposalReceiptResponse.EmployeeDto emp = h[3] != null
                    ? new DisposalReceiptResponse.EmployeeDto((String) h[3], (String) h[4], (String) h[5])
                    : null;

            List<Object[]> detailRows = em.createQuery(
                    "SELECT d.disposalDetailId, p.productId, p.productName, "
                            + "c.categoryName, p.unitPrice, d.disposedQuantity, d.disposalReason, p.expirationDate "
                            + "FROM DisposalReceiptDetail d JOIN Product p ON d.productId = p.productId "
                            + "LEFT JOIN ProductCategory c ON p.categoryId = c.categoryId WHERE d.voucherId = :voucherId",
                    Object[].class)
                    .setParameter("voucherId", voucherId.trim())
                    .getResultList();

            List<DisposalReceiptResponse.DetailItemDto> items = new ArrayList<>(detailRows.size());
            for (Object[] r : detailRows) {
                items.add(new DisposalReceiptResponse.DetailItemDto(
                        (String) r[0], (String) r[1], (String) r[2],
                        r[3] != null ? (String) r[3] : "Chung",
                        r[4] != null ? (BigDecimal) r[4] : BigDecimal.ZERO,
                        r[5] != null ? (Integer) r[5] : 0,
                        r[6] != null ? (String) r[6] : "",
                        formatIso(r[7])));
            }

            return Optional.of(new DisposalReceiptResponse.DetailResponse(
                    (String) h[0], totalAmount, formatIso(h[2]), emp, items.size(), items));
        });
    }

    private <R> R query(Function<EntityManager, R> action) {
        EntityManager em = getEntityManager();
        try {
            return action.apply(em);
        } finally {
            if (em.isOpen())
                em.close();
        }
    }

    private static String formatIso(Object val) {
        if (val == null)
            return null;
        if (val instanceof Timestamp ts)
            return ts.toLocalDateTime().atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
        if (val instanceof LocalDateTime ldt)
            return ldt.atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
        return val.toString();
    }
}
