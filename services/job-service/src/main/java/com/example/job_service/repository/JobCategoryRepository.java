package com.example.job_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.job_service.model.JobCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {
    List<JobCategory> findByDepartmentIdAndIsActiveTrue(Long departmentId);

    Optional<JobCategory> findByIdAndIsActiveTrue(Long id);
}
