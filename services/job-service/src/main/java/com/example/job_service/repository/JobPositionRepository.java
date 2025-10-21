package com.example.job_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.job_service.model.JobPosition;
import com.example.job_service.utils.enums.JobPositionStatus;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {
    List<JobPosition> findByStatus(JobPositionStatus status);

    List<JobPosition> findByStatusAndApplicationDeadlineAfter(JobPositionStatus status, LocalDate date);
}
