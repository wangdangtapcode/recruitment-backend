package com.example.candidate_service.repository;

import com.example.candidate_service.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByApplication_Id(Long applicationId);

    List<Review> findByReviewerId(Long reviewerId);

    List<Review> findByApplication_IdAndReviewerId(Long applicationId, Long reviewerId);

    @Query("SELECT r FROM Review r WHERE " +
            "(:applicationId IS NULL OR r.application.id = :applicationId) AND " +
            "(:reviewerId IS NULL OR r.reviewerId = :reviewerId) AND " +
            "(:startDate IS NULL OR r.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR r.createdAt <= :endDate)")
    Page<Review> findByFilters(@Param("applicationId") Long applicationId,
            @Param("reviewerId") Long reviewerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
