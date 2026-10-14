package com.mycompany.quanlysieuthi.modules.feedback;

import com.mycompany.quanlysieuthi.common.base.BaseRepository;
import com.mycompany.quanlysieuthi.entity.Feedback;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Repository for Feedback operations extending BaseRepository.
 */
public class FeedbackRepository extends BaseRepository<Feedback, String> {

    public FeedbackRepository() {
        super(Feedback.class);
    }

    public List<Feedback> findFeedbacks(int page, int limit, Integer rating,
                                        LocalDateTime fromDate, LocalDateTime toDate) {
        EntityManager em = getEntityManager();
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
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public FeedbackResponse.Summary getSummary(LocalDateTime fromDate, LocalDateTime toDate) {
        EntityManager em = getEntityManager();
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

            Query query = em.createQuery(jpql.toString());

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
