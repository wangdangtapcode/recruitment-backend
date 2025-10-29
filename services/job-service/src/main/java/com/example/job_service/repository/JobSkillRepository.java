package com.example.job_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.job_service.model.JobSkill;

import java.util.List;

@Repository
public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {
    Page<JobSkill> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT js FROM JobSkill js WHERE " +
            "(:isActive IS NULL OR js.isActive = :isActive) AND " +
            "(:keyword IS NULL OR LOWER(js.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(js.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<JobSkill> findByFilters(@Param("isActive") Boolean isActive,
            @Param("keyword") String keyword,
            Pageable pageable);
}
