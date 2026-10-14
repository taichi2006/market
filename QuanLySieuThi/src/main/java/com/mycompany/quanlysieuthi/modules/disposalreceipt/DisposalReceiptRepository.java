package com.mycompany.quanlysieuthi.modules.disposalreceipt;

import com.mycompany.quanlysieuthi.common.base.BaseRepository;
import com.mycompany.quanlysieuthi.config.JpaUtil;
import com.mycompany.quanlysieuthi.entity.DisposalReceipt;
import com.mycompany.quanlysieuthi.entity.DisposalReceiptDetail;
import com.mycompany.quanlysieuthi.entity.Product;
import com.mycompany.quanlysieuthi.entity.Voucher;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository handling database operations for Disposal Receipt module.
 */
public class DisposalReceiptRepository extends BaseRepository<DisposalReceipt, String> {

    public DisposalReceiptRepository() {
        super(DisposalReceipt.class);
    }

    public Optional<Product> findProductById(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Product.class, productId.trim()));
        } finally {
            em.close();
        }
    }

    public void saveDisposalReceiptTransaction(Voucher voucher,
                                               DisposalReceipt disposalReceipt,
                                               List<DisposalReceiptDetail> details,
                                               Map<String, Integer> productDeductionMap) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            em.persist(voucher);
            em.persist(disposalReceipt);

            if (details != null) {
                for (DisposalReceiptDetail detail : details) {
                    em.persist(detail);
                }
            }

            if (productDeductionMap != null) {
                for (Map.Entry<String, Integer> entry : productDeductionMap.entrySet()) {
                    String productId = entry.getKey();
                    int deductQty = entry.getValue();

                    Product product = em.find(Product.class, productId);
                    if (product != null) {
                        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                        product.setStockQuantity(Math.max(0, currentStock - deductQty));
                        em.merge(product);
                    }
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
            throw new RuntimeException("Lỗi giao dịch khi lưu phiếu hủy kho: " + root.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public List<DisposalReceiptResponse.ListItemDto> findDisposalReceipts(int page, int limit,
                                                                         String employeeId,
                                                                         LocalDateTime fromDateTime,
                                                                         LocalDateTime toDateTime) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT dr.voucherId, v.totalAmount, v.createdAt, " +
                    "e.employeeId, e.fullName, e.username, COUNT(d.disposalDetailId) " +
                    "FROM DisposalReceipt dr " +
                    "JOIN Voucher v ON dr.voucherId = v.voucherId " +
                    "LEFT JOIN Employee e ON v.employeeId = e.employeeId " +
                    "LEFT JOIN DisposalReceiptDetail d ON dr.voucherId = d.voucherId " +
                    "WHERE 1=1 ");

            if (employeeId != null && !employeeId.trim().isEmpty()) {
                jpql.append("AND v.employeeId = :employeeId ");
            }
            if (fromDateTime != null) {
                jpql.append("AND v.createdAt >= :fromDateTime ");
            }
            if (toDateTime != null) {
                jpql.append("AND v.createdAt <= :toDateTime ");
            }

            jpql.append("GROUP BY dr.voucherId, v.totalAmount, v.createdAt, e.employeeId, e.fullName, e.username ");
            jpql.append("ORDER BY v.createdAt DESC");

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

            int offset = (page - 1) * limit;
            query.setFirstResult(offset);
            query.setMaxResults(limit);

            List<Object[]> rawList = query.getResultList();
            List<DisposalReceiptResponse.ListItemDto> result = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            for (Object[] row : rawList) {
                String voucherId = (String) row[0];
                BigDecimal totalAmount = (BigDecimal) row[1];
                LocalDateTime createdAt = (LocalDateTime) row[2];
                String empId = (String) row[3];
                String empFullName = (String) row[4];
                String empUsername = (String) row[5];
                Long count = (Long) row[6];

                String formattedDate = createdAt != null
                        ? createdAt.atOffset(ZoneOffset.UTC).format(formatter)
                        : null;

                DisposalReceiptResponse.EmployeeDto empDto = null;
                if (empId != null) {
                    empDto = new DisposalReceiptResponse.EmployeeDto(empId, empFullName, empUsername);
                }

                result.add(new DisposalReceiptResponse.ListItemDto(
                        voucherId,
                        totalAmount != null ? totalAmount : BigDecimal.ZERO,
                        formattedDate,
                        empDto,
                        count != null ? count.intValue() : 0));
            }

            return result;
        } finally {
            em.close();
        }
    }

    public Optional<DisposalReceiptResponse.DetailResponse> findDisposalReceiptDetailById(String voucherId) {
        if (voucherId == null || voucherId.trim().isEmpty()) {
            return Optional.empty();
        }

        EntityManager em = JpaUtil.getEntityManager();
        try {
            String headerJpql = "SELECT dr.voucherId, v.totalAmount, v.createdAt, " +
                    "e.employeeId, e.fullName, e.username " +
                    "FROM DisposalReceipt dr " +
                    "JOIN Voucher v ON dr.voucherId = v.voucherId " +
                    "LEFT JOIN Employee e ON v.employeeId = e.employeeId " +
                    "WHERE dr.voucherId = :voucherId";

            TypedQuery<Object[]> headerQuery = em.createQuery(headerJpql, Object[].class);
            headerQuery.setParameter("voucherId", voucherId.trim());

            List<Object[]> headerList = headerQuery.getResultList();
            if (headerList.isEmpty()) {
                return Optional.empty();
            }

            Object[] headerRow = headerList.get(0);
            String vId = (String) headerRow[0];
            BigDecimal totalAmount = (BigDecimal) headerRow[1];
            LocalDateTime createdAt = (LocalDateTime) headerRow[2];
            String empId = (String) headerRow[3];
            String empFullName = (String) headerRow[4];
            String empUsername = (String) headerRow[5];

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            String formattedDate = createdAt != null
                    ? createdAt.atOffset(ZoneOffset.UTC).format(formatter)
                    : null;

            DisposalReceiptResponse.EmployeeDto empDto = null;
            if (empId != null) {
                empDto = new DisposalReceiptResponse.EmployeeDto(empId, empFullName, empUsername);
            }

            String detailJpql = "SELECT d.disposalDetailId, p.productId, p.productName, " +
                    "c.categoryName, p.unitPrice, d.disposedQuantity, d.disposalReason, p.expirationDate " +
                    "FROM DisposalReceiptDetail d " +
                    "JOIN Product p ON d.productId = p.productId " +
                    "LEFT JOIN ProductCategory c ON p.categoryId = c.categoryId " +
                    "WHERE d.voucherId = :voucherId";

            TypedQuery<Object[]> detailQuery = em.createQuery(detailJpql, Object[].class);
            detailQuery.setParameter("voucherId", voucherId.trim());

            List<Object[]> detailRows = detailQuery.getResultList();
            List<DisposalReceiptResponse.DetailItemDto> items = new ArrayList<>();

            for (Object[] r : detailRows) {
                String detailId = (String) r[0];
                String prodId = (String) r[1];
                String prodName = (String) r[2];
                String catName = (String) r[3];
                BigDecimal price = (BigDecimal) r[4];
                Integer disposedQty = (Integer) r[5];
                String reason = (String) r[6];
                LocalDateTime expDate = (LocalDateTime) r[7];

                String formattedExp = expDate != null
                        ? expDate.atOffset(ZoneOffset.UTC).format(formatter)
                        : null;

                items.add(new DisposalReceiptResponse.DetailItemDto(
                        detailId,
                        prodId,
                        prodName,
                        catName != null ? catName : "Chung",
                        price != null ? price : BigDecimal.ZERO,
                        disposedQty != null ? disposedQty : 0,
                        reason != null ? reason : "",
                        formattedExp));
            }

            return Optional.of(new DisposalReceiptResponse.DetailResponse(
                    vId,
                    totalAmount != null ? totalAmount : BigDecimal.ZERO,
                    formattedDate,
                    empDto,
                    items.size(),
                    items));
        } finally {
            em.close();
        }
    }
}
