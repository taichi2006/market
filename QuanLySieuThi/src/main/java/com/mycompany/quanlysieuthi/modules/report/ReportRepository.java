package com.mycompany.quanlysieuthi.modules.report;

import com.mycompany.quanlysieuthi.common.base.BaseRepository;
import com.mycompany.quanlysieuthi.config.JpaUtil;
import com.mycompany.quanlysieuthi.entity.InventoryReport;
import com.mycompany.quanlysieuthi.entity.InventoryReportDetail;
import com.mycompany.quanlysieuthi.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository handling database operations for Report module.
 */
public class ReportRepository extends BaseRepository<InventoryReport, String> {

    public static class AuditMeta implements Serializable {
        private static final long serialVersionUID = 1L;
        private String auditTitle;
        private String note;
        private Map<String, String> itemReasons = new ConcurrentHashMap<>();

        public AuditMeta() {
        }

        public AuditMeta(String auditTitle, String note) {
            this.auditTitle = auditTitle;
            this.note = note;
        }

        public String getAuditTitle() {
            return auditTitle;
        }

        public void setAuditTitle(String auditTitle) {
            this.auditTitle = auditTitle;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public Map<String, String> getItemReasons() {
            return itemReasons;
        }

        public void setItemReasons(Map<String, String> itemReasons) {
            this.itemReasons = itemReasons;
        }
    }

    private static final Map<String, AuditMeta> AUDIT_META_CACHE = new ConcurrentHashMap<>();
    private static final File META_FILE = new File(System.getProperty("java.io.tmpdir"),
            "quanlysieuthi_audit_meta.ser");

    static {
        loadAuditMetaFile();
    }

    private static synchronized void loadAuditMetaFile() {
        if (META_FILE.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(META_FILE))) {
                Object obj = ois.readObject();
                if (obj instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) obj;
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (entry.getKey() instanceof String && entry.getValue() instanceof AuditMeta) {
                            AUDIT_META_CACHE.put((String) entry.getKey(), (AuditMeta) entry.getValue());
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static synchronized void persistAuditMetaFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(META_FILE))) {
            oos.writeObject(new HashMap<>(AUDIT_META_CACHE));
        } catch (Exception ignored) {
        }
    }

    public static void saveAuditMeta(String reportId, String title, String note, Map<String, String> reasons) {
        if (reportId == null)
            return;
        AuditMeta meta = new AuditMeta(title, note);
        if (reasons != null) {
            meta.getItemReasons().putAll(reasons);
        }
        AUDIT_META_CACHE.put(reportId, meta);
        persistAuditMetaFile();
    }

    public static AuditMeta getAuditMeta(String reportId) {
        if (reportId == null)
            return null;
        return AUDIT_META_CACHE.get(reportId);
    }

    public ReportRepository() {
        super(InventoryReport.class);
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

    public void saveAuditReportTransaction(InventoryReport report, List<InventoryReportDetail> details) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            em.persist(report);

            if (details != null) {
                for (InventoryReportDetail detail : details) {
                    em.persist(detail);
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
            throw new RuntimeException("Lỗi giao dịch khi lưu biên bản kiểm kê kho: " + root.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public List<ReportResponse.ListItemDto> findAuditReports(int page, int limit,
            String employeeId,
            LocalDateTime fromDateTime,
            LocalDateTime toDateTime) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT ir.reportId, ir.reportDate, e.employeeId, e.fullName, e.username, " +
                            "SUM(CASE WHEN d.actualQuantity <> d.systemQuantity THEN 1L ELSE 0L END) " +
                            "FROM InventoryReport ir " +
                            "LEFT JOIN Employee e ON ir.employeeId = e.employeeId " +
                            "LEFT JOIN InventoryReportDetail d ON ir.reportId = d.reportId " +
                            "WHERE 1=1 ");

            if (employeeId != null && !employeeId.trim().isEmpty()) {
                jpql.append("AND ir.employeeId = :employeeId ");
            }
            if (fromDateTime != null) {
                jpql.append("AND ir.reportDate >= :fromDateTime ");
            }
            if (toDateTime != null) {
                jpql.append("AND ir.reportDate <= :toDateTime ");
            }

            jpql.append("GROUP BY ir.reportId, ir.reportDate, e.employeeId, e.fullName, e.username ");
            jpql.append("ORDER BY ir.reportDate DESC");

            TypedQuery<Object[]> query = em.createQuery(jpql.toString(), Object[].class);

            if (employeeId != null && !employeeId.trim().isEmpty()) {
                query.setParameter("employeeId", employeeId.trim());
            }
            if (fromDateTime != null) {
                query.setParameter("fromDateTime", fromDateTime);
            }
            if (toDateTime != null) {
                query.setParameter("toDateTime", toDateTime);
            }

            if (limit > 0) {
                int offset = (page - 1) * limit;
                query.setFirstResult(offset);
                query.setMaxResults(limit);
            }

            List<Object[]> rawList = query.getResultList();
            List<ReportResponse.ListItemDto> result = new ArrayList<>();
            DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            DateTimeFormatter titleFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (Object[] row : rawList) {
                String reportId = (String) row[0];
                LocalDateTime reportDate = (LocalDateTime) row[1];
                String empId = (String) row[2];
                String empFullName = (String) row[3];
                String empUsername = (String) row[4];
                int totalDiscrepancyItems = (row[5] instanceof Number) ? ((Number) row[5]).intValue() : 0;

                String formattedDate = reportDate != null
                        ? reportDate.atOffset(ZoneOffset.UTC).format(isoFormatter)
                        : null;

                AuditMeta meta = getAuditMeta(reportId);
                String title = (meta != null && meta.getAuditTitle() != null && !meta.getAuditTitle().trim().isEmpty())
                        ? meta.getAuditTitle().trim()
                        : ("Kiểm kê kho định kỳ " + (reportDate != null ? reportDate.format(titleFormatter) : ""));

                ReportResponse.EmployeeDto empDto = null;
                if (empId != null) {
                    empDto = new ReportResponse.EmployeeDto(empId, empFullName, empUsername);
                }

                result.add(new ReportResponse.ListItemDto(
                        reportId,
                        title,
                        formattedDate,
                        totalDiscrepancyItems,
                        empDto));
            }

            return result;
        } finally {
            em.close();
        }
    }

    public Optional<ReportResponse.DetailResponse> findAuditReportDetailById(String reportId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String reportJpql = "SELECT ir.reportId, ir.reportDate, e.employeeId, e.fullName, e.username " +
                    "FROM InventoryReport ir " +
                    "LEFT JOIN Employee e ON ir.employeeId = e.employeeId " +
                    "WHERE ir.reportId = :reportId";

            List<Object[]> reportRows = em.createQuery(reportJpql, Object[].class)
                    .setParameter("reportId", reportId)
                    .getResultList();

            if (reportRows.isEmpty()) {
                return Optional.empty();
            }

            Object[] reportRow = reportRows.get(0);
            LocalDateTime reportDate = (LocalDateTime) reportRow[1];
            String empId = (String) reportRow[2];
            String empFullName = (String) reportRow[3];
            String empUsername = (String) reportRow[4];

            ReportResponse.EmployeeDto empDto = null;
            if (empId != null) {
                empDto = new ReportResponse.EmployeeDto(empId, empFullName, empUsername);
            }

            DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            DateTimeFormatter titleFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            String createdAt = reportDate != null
                    ? reportDate.atOffset(ZoneOffset.UTC).format(isoFormatter)
                    : null;

            AuditMeta meta = getAuditMeta(reportId);
            String auditTitle = (meta != null && meta.getAuditTitle() != null && !meta.getAuditTitle().trim().isEmpty())
                    ? meta.getAuditTitle().trim()
                    : ("Kiểm kê kho định kỳ " + (reportDate != null ? reportDate.format(titleFormatter) : ""));
            String note = (meta != null && meta.getNote() != null && !meta.getNote().trim().isEmpty())
                    ? meta.getNote().trim()
                    : "Biên bản kiểm kê kho định kỳ";

            // Sử dụng View dbo.vw_InventoryAuditDiscrepancy đã được đóng gói logic tính
            // toán sai lệch và phân loại
            String viewSql = "SELECT report_id, product_id, product_name, category_name, " +
                    "system_quantity, actual_quantity, discrepancy_quantity, discrepancy_status, discrepancy_amount " +
                    "FROM dbo.vw_InventoryAuditDiscrepancy WHERE report_id = :reportId";

            List<?> detailRows = em.createNativeQuery(viewSql)
                    .setParameter("reportId", reportId)
                    .getResultList();

            List<ReportResponse.DetailItemDto> items = new ArrayList<>();
            int totalDiscrepancyItems = 0;

            for (Object obj : detailRows) {
                Object[] dRow = (Object[]) obj;
                String rId = (String) dRow[0];
                String pId = (String) dRow[1];
                String pName = (String) dRow[2];
                String cName = (String) dRow[3];
                Integer sysQty = dRow[4] != null ? ((Number) dRow[4]).intValue() : 0;
                Integer actQty = dRow[5] != null ? ((Number) dRow[5]).intValue() : 0;
                Integer diff = dRow[6] != null ? ((Number) dRow[6]).intValue() : 0;

                String status;
                if (diff < 0) {
                    status = "SHORTAGE";
                    totalDiscrepancyItems++;
                } else if (diff > 0) {
                    status = "SURPLUS";
                    totalDiscrepancyItems++;
                } else {
                    status = "MATCH";
                }

                String customReason = (meta != null && meta.getItemReasons() != null)
                        ? meta.getItemReasons().get(pId)
                        : null;
                String reason;
                if (customReason != null && !customReason.trim().isEmpty()) {
                    reason = customReason.trim();
                } else if (diff < 0) {
                    reason = "Thiếu " + Math.abs(diff) + " sản phẩm so với tồn kho hệ thống";
                } else if (diff > 0) {
                    reason = "Thừa " + diff + " sản phẩm so với tồn kho hệ thống";
                } else {
                    reason = "Khớp số lượng tồn kho";
                }

                String detailId = rId + "_" + pId;

                items.add(new ReportResponse.DetailItemDto(
                        detailId,
                        pId,
                        pName != null ? pName : "",
                        cName != null ? cName : "",
                        sysQty,
                        actQty,
                        diff,
                        status,
                        reason));
            }

            return Optional.of(new ReportResponse.DetailResponse(
                    reportId,
                    auditTitle,
                    note,
                    createdAt,
                    empDto,
                    totalDiscrepancyItems,
                    items));
        } finally {
            em.close();
        }
    }

    /**
     * Khởi tạo một phiên kiểm kê kho mới cho toàn bộ một danh mục hàng hóa
     * thông qua Stored Procedure dbo.sp_CreateInventoryAuditSession.
     * Tự động snapshot số lượng tồn hệ thống của tất cả sản phẩm thuộc danh mục.
     */
    public int createInventoryAuditSession(String reportId, String employeeId, String categoryId) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            jakarta.persistence.StoredProcedureQuery sp = em
                    .createStoredProcedureQuery("dbo.sp_CreateInventoryAuditSession");
            sp.registerStoredProcedureParameter("ReportId", String.class, jakarta.persistence.ParameterMode.IN);
            sp.registerStoredProcedureParameter("EmployeeId", String.class, jakarta.persistence.ParameterMode.IN);
            sp.registerStoredProcedureParameter("CategoryId", String.class, jakarta.persistence.ParameterMode.IN);
            sp.registerStoredProcedureParameter("TotalItemsCreated", Integer.class,
                    jakarta.persistence.ParameterMode.OUT);

            sp.setParameter("ReportId", reportId);
            sp.setParameter("EmployeeId", employeeId);
            sp.setParameter("CategoryId", categoryId);

            sp.execute();
            Integer total = (Integer) sp.getOutputParameterValue("TotalItemsCreated");
            tx.commit();
            return total != null ? total : 0;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            throw new RuntimeException("Lỗi khởi tạo phiên kiểm kê: " + root.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Chốt đợt kiểm kê kho và tự động xuất hủy hàng hao hụt
     * thông qua Stored Procedure dbo.sp_FinalizeAuditAndAutoDispose.
     * Cập nhật tồn kho theo số lượng thực tế kiểm kê và tự động sinh voucher/phiếu
     * xuất hủy nếu có thiếu hụt.
     */
    public void finalizeAuditAndAutoDispose(String reportId, String disposalVoucherId, String employeeId) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            jakarta.persistence.StoredProcedureQuery sp = em
                    .createStoredProcedureQuery("dbo.sp_FinalizeAuditAndAutoDispose");
            sp.registerStoredProcedureParameter("ReportId", String.class, jakarta.persistence.ParameterMode.IN);
            sp.registerStoredProcedureParameter("DisposalVoucherId", String.class,
                    jakarta.persistence.ParameterMode.IN);
            sp.registerStoredProcedureParameter("EmployeeId", String.class, jakarta.persistence.ParameterMode.IN);

            sp.setParameter("ReportId", reportId);
            sp.setParameter("DisposalVoucherId", disposalVoucherId);
            sp.setParameter("EmployeeId", employeeId);

            sp.execute();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            throw new RuntimeException("Lỗi chốt kiểm kê và tạo phiếu xuất hủy: " + root.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Báo cáo tình hình kho theo danh mục từ View
     * dbo.vw_ProductCategoryInventorySummary.
     */
    public List<Map<String, Object>> findCategoryInventoryReport() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String sql = "SELECT category_id, category_name, total_products, total_stock_quantity, " +
                    "total_inventory_value, low_stock_product_count, expiring_soon_product_count " +
                    "FROM dbo.vw_ProductCategoryInventorySummary ORDER BY total_inventory_value DESC";
            Query q = em.createNativeQuery(sql);
            List<?> rows = q.getResultList();
            List<Map<String, Object>> list = new ArrayList<>();
            for (Object obj : rows) {
                Object[] r = (Object[]) obj;
                Map<String, Object> map = new HashMap<>();
                map.put("categoryId", r[0]);
                map.put("categoryName", r[1]);
                map.put("totalProducts", r[2]);
                map.put("totalStockQuantity", r[3]);
                map.put("totalInventoryValue", r[4]);
                map.put("lowStockProductCount", r[5]);
                map.put("expiringSoonProductCount", r[6]);
                list.add(map);
            }
            return list;
        } finally {
            em.close();
        }
    }

}
