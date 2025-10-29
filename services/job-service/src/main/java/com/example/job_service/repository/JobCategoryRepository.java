package com.example.job_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.job_service.model.JobCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {
    List<JobCategory> findByDepartmentIdAndIsActiveTrue(Long departmentId);

    Optional<JobCategory> findByIdAndIsActiveTrue(Long id);

    @Query("SELECT jc FROM JobCategory jc WHERE " +
            "(:departmentId IS NULL OR jc.departmentId = :departmentId) AND " +
            "(:keyword IS NULL OR LOWER(jc.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(jc.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<JobCategory> findByFilters(@Param("departmentId") Long departmentId,
            @Param("keyword") String keyword,
            Pageable pageable);
}
