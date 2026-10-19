package com.mycompany.quanlysieuthi.modules.feedback;

import com.mycompany.quanlysieuthi.common.base.BaseRepository;
import com.mycompany.quanlysieuthi.entity.Feedback;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Repository for Feedback operations.
 * Tận dụng các tính năng CSDL nâng cao:
 * - View: dbo.v_danh_gia_khach_hang (dành cho Admin/Store Owner hiển thị danh sách đánh giá)
 * - Ràng buộc CHECK: chk_feedback_rating (rating BETWEEN 1 AND 5)
 * - Phân quyền: role_customer (chỉ INSERT), role_owner (toàn quyền)
 */
public class FeedbackRepository extends BaseRepository<Feedback, String> {

    public FeedbackRepository() {
        super(Feedback.class);
    }

    @Override
    public Feedback save(Feedback entity) {
        try {
            return super.save(entity);
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            String msg = root.getMessage();
            if (msg != null && msg.contains("chk_feedback_rating")) {
                throw new IllegalArgumentException("rating không nằm trong khoảng từ 1 đến 5 (vi phạm ràng buộc chk_feedback_rating).");
            }
            throw e;
        }
    }

    /**
     * Lấy danh sách đánh giá thông qua Khung nhìn (View) dbo.v_danh_gia_khach_hang.
     */
    public List<Feedback> findFeedbacks(int page, int limit, Integer rating,
                                        LocalDateTime fromDate, LocalDateTime toDate) {
        EntityManager em = getEntityManager();
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT feedback_id, customer_name, phone_number, rating, comment, created_at " +
                    "FROM dbo.v_danh_gia_khach_hang WHERE 1=1 ");

            if (rating != null) {
                sql.append("AND rating = :rating ");
            }
            if (fromDate != null) {
                sql.append("AND created_at >= :fromDate ");
            }
            if (toDate != null) {
                sql.append("AND created_at <= :toDate ");
            }

            sql.append("ORDER BY created_at DESC");

            Query query = em.createNativeQuery(sql.toString());

            if (rating != null) {
                query.setParameter("rating", rating);
            }
            if (fromDate != null) {
                query.setParameter("fromDate", fromDate);
            }
            if (toDate != null) {
                query.setParameter("toDate", toDate);
            }

            int offset = (page - 1) * limit;
            query.setFirstResult(offset);
            query.setMaxResults(limit);

            @SuppressWarnings("unchecked")
            List<Object[]> rows = query.getResultList();
            List<Feedback> result = new ArrayList<>(rows.size());
            for (Object[] r : rows) {
                LocalDateTime createdAt = null;
                if (r[5] instanceof Timestamp ts) {
                    createdAt = ts.toLocalDateTime();
                } else if (r[5] instanceof LocalDateTime ldt) {
                    createdAt = ldt;
                }
                Integer rRating = r[3] != null ? ((Number) r[3]).intValue() : null;
                result.add(new Feedback((String) r[0], (String) r[1], (String) r[2], rRating, (String) r[4], createdAt));
            }
            return result;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Tìm chi tiết đánh giá thông qua Khung nhìn (View) dbo.v_danh_gia_khach_hang.
     */
    @Override
    public Optional<Feedback> findById(String feedbackId) {
        if (feedbackId == null || feedbackId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = getEntityManager();
        try {
            String sql = "SELECT feedback_id, customer_name, phone_number, rating, comment, created_at " +
                         "FROM dbo.v_danh_gia_khach_hang WHERE feedback_id = :id";
            Query query = em.createNativeQuery(sql);
            query.setParameter("id", feedbackId.trim());

            @SuppressWarnings("unchecked")
            List<Object[]> rows = query.getResultList();
            if (rows.isEmpty()) {
                return Optional.empty();
            }
            Object[] r = rows.get(0);
            LocalDateTime createdAt = null;
            if (r[5] instanceof Timestamp ts) {
                createdAt = ts.toLocalDateTime();
            } else if (r[5] instanceof LocalDateTime ldt) {
                createdAt = ldt;
            }
            Integer rRating = r[3] != null ? ((Number) r[3]).intValue() : null;
            return Optional.of(new Feedback((String) r[0], (String) r[1], (String) r[2], rRating, (String) r[4], createdAt));
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Tổng hợp thống kê đánh giá thông qua Khung nhìn (View) dbo.v_danh_gia_khach_hang.
     */
    public FeedbackResponse.Summary getSummary(LocalDateTime fromDate, LocalDateTime toDate) {
        EntityManager em = getEntityManager();
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT " +
                    "  COUNT(1), " +
                    "  AVG(CAST(rating AS FLOAT)), " +
                    "  SUM(CASE WHEN rating = 5 THEN 1 ELSE 0 END), " +
                    "  SUM(CASE WHEN rating = 4 THEN 1 ELSE 0 END), " +
                    "  SUM(CASE WHEN rating = 3 THEN 1 ELSE 0 END), " +
                    "  SUM(CASE WHEN rating = 2 THEN 1 ELSE 0 END), " +
                    "  SUM(CASE WHEN rating = 1 THEN 1 ELSE 0 END) " +
                    "FROM dbo.v_danh_gia_khach_hang WHERE 1=1 "
            );

            if (fromDate != null) {
                sql.append("AND created_at >= :fromDate ");
            }
            if (toDate != null) {
                sql.append("AND created_at <= :toDate ");
            }

            Query query = em.createNativeQuery(sql.toString());

            if (fromDate != null) {
                query.setParameter("fromDate", fromDate);
            }
            if (toDate != null) {
                query.setParameter("toDate", toDate);
            }

            Object[] row = (Object[]) query.getSingleResult();

            long total = row[0] != null ? ((Number) row[0]).longValue() : 0L;
            double avg = 0.0;
            if (row[1] != null) {
                double rawAvg = ((Number) row[1]).doubleValue();
                avg = Math.round(rawAvg * 10.0) / 10.0;
            }

            long fiveStar = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            long fourStar = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            long threeStar = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            long twoStar = row[5] != null ? ((Number) row[5]).longValue() : 0L;
            long oneStar = row[6] != null ? ((Number) row[6]).longValue() : 0L;

            FeedbackResponse.RatingBreakdown breakdown = new FeedbackResponse.RatingBreakdown(
                    fiveStar, fourStar, threeStar, twoStar, oneStar
            );

            return new FeedbackResponse.Summary(total, avg, breakdown);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}

