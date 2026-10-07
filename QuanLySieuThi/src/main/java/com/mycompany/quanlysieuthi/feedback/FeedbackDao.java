package com.mycompany.quanlysieuthi.feedback;

import com.mycompany.quanlysieuthi.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Feedback operations.
 */
public class FeedbackDao {

    public Optional<Feedback> findById(String feedbackId) {
        if (feedbackId == null || feedbackId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Feedback feedback = em.find(Feedback.class, feedbackId.trim());
            return Optional.ofNullable(feedback);
        } finally {
            em.close();
        }
    }

    public Feedback save(Feedback feedback) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Feedback result = em.merge(feedback);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi khi lưu Feedback: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public List<Feedback> findFeedbacks(int page, int limit, Integer rating,
                                        LocalDateTime fromDate, LocalDateTime toDate) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT f FROM Feedback f WHERE 1=1 ");

            if (rating != null) {
                jpql.append("AND f.rating = :rating ");
            }
            if (fromDate != null) {
                jpql.append("AND f.createdAt >= :fromDate ");
            }
            if (toDate != null) {
                jpql.append("AND f.createdAt <= :toDate ");
            }

            jpql.append("ORDER BY f.createdAt DESC");

            TypedQuery<Feedback> query = em.createQuery(jpql.toString(), Feedback.class);

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

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public FeedbackSummaryDto getSummary(LocalDateTime fromDate, LocalDateTime toDate) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT " +
                    "  COUNT(f), " +
                    "  AVG(f.rating), " +
                    "  SUM(CASE WHEN f.rating = 5 THEN 1L ELSE 0L END), " +
                    "  SUM(CASE WHEN f.rating = 4 THEN 1L ELSE 0L END), " +
                    "  SUM(CASE WHEN f.rating = 3 THEN 1L ELSE 0L END), " +
                    "  SUM(CASE WHEN f.rating = 2 THEN 1L ELSE 0L END), " +
                    "  SUM(CASE WHEN f.rating = 1 THEN 1L ELSE 0L END) " +
                    "FROM Feedback f WHERE 1=1 "
            );

            if (fromDate != null) {
                jpql.append("AND f.createdAt >= :fromDate ");
            }
            if (toDate != null) {
                jpql.append("AND f.createdAt <= :toDate ");
            }

            jakarta.persistence.Query query = em.createQuery(jpql.toString());

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

            FeedbackRatingBreakdownDto breakdown = new FeedbackRatingBreakdownDto(
                    fiveStar, fourStar, threeStar, twoStar, oneStar
            );

            return new FeedbackSummaryDto(total, avg, breakdown);
        } finally {
            em.close();
        }
    }
}
