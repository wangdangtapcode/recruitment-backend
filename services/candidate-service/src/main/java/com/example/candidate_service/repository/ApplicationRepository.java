package com.example.candidate_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.candidate_service.model.Application;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

        List<Application> findByCandidateId(Long candidateId);

        List<Application> findByJobPositionId(Long jobPositionId);

        boolean existsByEmailAndJobPositionId(String email, Long jobPositionId);

        Optional<Application> findByEmailAndJobPositionId(String email, Long jobPositionId);

        @Query("SELECT a FROM Application a WHERE " +
                        "(:jobPositionId IS NULL OR a.jobPositionId = :jobPositionId) AND " +
                        "(:status IS NULL OR a.status = :status) AND " +
                        "(:keyword IS NULL OR LOWER(a.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(a.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(a.feedback) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<Application> findByFilters(
                        @Param("jobPositionId") Long jobPositionId,
                        @Param("status") String status,
                        @Param("keyword") String keyword,
                        Pageable pageable);
}
