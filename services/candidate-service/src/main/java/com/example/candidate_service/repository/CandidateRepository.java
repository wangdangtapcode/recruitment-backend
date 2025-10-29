package com.example.candidate_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.candidate_service.model.Candidate;
import com.example.candidate_service.utils.enums.CandidateStage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    Optional<Candidate> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT c FROM Candidate c WHERE (:stage IS NULL OR c.stage = :stage) AND (:keyword IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Candidate> findByFilters(@Param("stage") CandidateStage stage,
            @Param("keyword") String keyword,
            Pageable pageable);
}
