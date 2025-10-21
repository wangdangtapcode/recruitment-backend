package com.example.job_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.job_service.model.RecruitmentRequest;
import com.example.job_service.utils.enums.RecruitmentRequestStatus;

import java.util.List;

@Repository
public interface RecruitmentRequestRepository extends JpaRepository<RecruitmentRequest, Long> {
    List<RecruitmentRequest> findByRequesterId(Long requesterId);

    List<RecruitmentRequest> findByStatus(RecruitmentRequestStatus status);

    List<RecruitmentRequest> findByDepartmentId(Long departmentId);

    List<RecruitmentRequest> findAllByIsActiveTrue();
}
