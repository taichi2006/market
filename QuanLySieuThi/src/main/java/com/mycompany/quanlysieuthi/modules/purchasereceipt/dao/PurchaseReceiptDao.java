package com.mycompany.quanlysieuthi.modules.purchasereceipt.dao;

import com.mycompany.quanlysieuthi.config.JpaUtil;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.entity.Product;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.entity.ProductCategory;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.entity.PurchaseReceipt;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.entity.PurchaseReceiptDetail;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.entity.Voucher;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.dto.response.PurchaseReceiptDetailResponse;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.dto.response.PurchaseReceiptEmployeeDto;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.dto.response.PurchaseReceiptItemDetailDto;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.dto.response.PurchaseReceiptListItemDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Purchase Receipt transactions.
 */
public class PurchaseReceiptDao {

    public boolean existsCategory(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            ProductCategory category = em.find(ProductCategory.class, categoryId.trim());
            return category != null;
        } finally {
            em.close();
        }
    }

    public void savePurchaseReceiptTransaction(Voucher voucher,
            PurchaseReceipt purchaseReceipt,
            List<Product> products,
            List<PurchaseReceiptDetail> details) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            em.persist(voucher);
            em.persist(purchaseReceipt);

            for (Product product : products) {
                em.persist(product);
            }

            for (PurchaseReceiptDetail detail : details) {
                em.persist(detail);
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
            throw new RuntimeException("Lỗi giao dịch khi lưu phiếu nhập hàng: " + root.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public List<PurchaseReceiptListItemDto> findPurchaseReceipts(int page, int limit,
            String supplierName,
            String employeeId,
            LocalDateTime fromDateTime,
            LocalDateTime toDateTime) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT pr.voucherId, pr.supplierName, v.totalAmount, v.createdAt, " +
                            "e.employeeId, e.fullName, e.username " +
                            "FROM PurchaseReceipt pr " +
                            "JOIN Voucher v ON pr.voucherId = v.voucherId " +
                            "LEFT JOIN Employee e ON v.employeeId = e.employeeId " +
                            "WHERE 1=1 ");

            if (supplierName != null && !supplierName.trim().isEmpty()) {
                jpql.append("AND LOWER(pr.supplierName) LIKE LOWER(:supplierName) ");
            }
            if (employeeId != null && !employeeId.trim().isEmpty()) {
                jpql.append("AND v.employeeId = :employeeId ");
            }
            if (fromDateTime != null) {
                jpql.append("AND v.createdAt >= :fromDateTime ");
            }
            if (toDateTime != null) {
                jpql.append("AND v.createdAt <= :toDateTime ");
            }

            jpql.append("ORDER BY v.createdAt DESC");

            TypedQuery<Object[]> query = em.createQuery(jpql.toString(), Object[].class);

            if (supplierName != null && !supplierName.trim().isEmpty()) {
                query.setParameter("supplierName", "%" + supplierName.trim() + "%");
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

            List<Object[]> rawList = query.getResultList();
            List<PurchaseReceiptListItemDto> result = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            for (Object[] row : rawList) {
                String voucherId = (String) row[0];
                String sName = (String) row[1];
                BigDecimal totalAmount = (BigDecimal) row[2];
                LocalDateTime createdAt = (LocalDateTime) row[3];
                String empId = (String) row[4];
                String empFullName = (String) row[5];
                String empUsername = (String) row[6];

                String formattedDate = createdAt != null
                        ? createdAt.atOffset(ZoneOffset.UTC).format(formatter)
                        : null;

                PurchaseReceiptEmployeeDto empDto = null;
                if (empId != null) {
                    empDto = new PurchaseReceiptEmployeeDto(empId, empFullName, empUsername);
                }

                result.add(new PurchaseReceiptListItemDto(
                        voucherId,
                        sName,
                        totalAmount,
                        formattedDate,
                        empDto));
            }

            return result;
        } finally {
            em.close();
        }
    }

    public Optional<PurchaseReceiptDetailResponse> findPurchaseReceiptDetailById(String voucherId) {
        if (voucherId == null || voucherId.trim().isEmpty()) {
            return Optional.empty();
        }

        EntityManager em = JpaUtil.getEntityManager();
        try {
            String headerJpql = "SELECT pr.voucherId, pr.supplierName, v.totalAmount, v.createdAt, " +
                    "e.employeeId, e.fullName, e.username " +
                    "FROM PurchaseReceipt pr " +
                    "JOIN Voucher v ON pr.voucherId = v.voucherId " +
                    "LEFT JOIN Employee e ON v.employeeId = e.employeeId " +
                    "WHERE pr.voucherId = :voucherId";

            List<Object[]> headerList = em.createQuery(headerJpql, Object[].class)
                    .setParameter("voucherId", voucherId.trim())
                    .getResultList();

            if (headerList.isEmpty()) {
                return Optional.empty();
            }

            Object[] headerRow = headerList.get(0);
            String vId = (String) headerRow[0];
            String sName = (String) headerRow[1];
            BigDecimal totalAmount = (BigDecimal) headerRow[2];
            LocalDateTime createdAt = (LocalDateTime) headerRow[3];
            String empId = (String) headerRow[4];
            String empFullName = (String) headerRow[5];
            String empUsername = (String) headerRow[6];

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            String formattedDate = createdAt != null
                    ? createdAt.atOffset(ZoneOffset.UTC).format(formatter)
                    : null;

            PurchaseReceiptEmployeeDto empDto = null;
            if (empId != null) {
                empDto = new PurchaseReceiptEmployeeDto(empId, empFullName, empUsername);
            }

            String itemsJpql = "SELECT d.purchaseDetailId, p.productId, p.productName, c.categoryName, " +
                    "d.purchasedQuantity, d.purchasePrice, d.expirationDate " +
                    "FROM PurchaseReceiptDetail d " +
                    "JOIN Product p ON d.productId = p.productId " +
                    "LEFT JOIN ProductCategory c ON p.categoryId = c.categoryId " +
                    "WHERE d.voucherId = :voucherId " +
                    "ORDER BY d.purchaseDetailId ASC";

            List<Object[]> itemRows = em.createQuery(itemsJpql, Object[].class)
                    .setParameter("voucherId", voucherId.trim())
                    .getResultList();

            List<PurchaseReceiptItemDetailDto> items = new ArrayList<>();
            for (Object[] itemRow : itemRows) {
                String detailId = (String) itemRow[0];
                String prodId = (String) itemRow[1];
                String prodName = (String) itemRow[2];
                String catName = (String) itemRow[3];
                Integer qty = (Integer) itemRow[4];
                BigDecimal price = (BigDecimal) itemRow[5];
                LocalDateTime expDate = (LocalDateTime) itemRow[6];

                BigDecimal lineTotal = (qty != null && price != null)
                        ? price.multiply(BigDecimal.valueOf(qty))
                        : BigDecimal.ZERO;

                String formattedExpDate = expDate != null
                        ? expDate.atOffset(ZoneOffset.UTC).format(formatter)
                        : null;

                items.add(new PurchaseReceiptItemDetailDto(
                        detailId,
                        prodId,
                        prodName,
                        catName,
                        qty,
                        price,
                        lineTotal,
                        formattedExpDate));
            }

            return Optional.of(new PurchaseReceiptDetailResponse(
                    vId,
                    sName,
                    totalAmount,
                    formattedDate,
                    empDto,
                    items));
        } finally {
            em.close();
        }
    }
}
