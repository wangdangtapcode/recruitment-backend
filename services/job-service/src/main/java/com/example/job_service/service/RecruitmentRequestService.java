package com.example.job_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.job_service.dto.recruitment.ApproveRecruitmentRequestDTO;
import com.example.job_service.dto.recruitment.CreateRecruitmentRequestDTO;
import com.example.job_service.model.JobCategory;
import com.example.job_service.model.RecruitmentRequest;
import com.example.job_service.repository.RecruitmentRequestRepository;
import com.example.job_service.utils.enums.RecruitmentRequestStatus;

@Service
public class RecruitmentRequestService {
    private final RecruitmentRequestRepository recruitmentRequestRepository;
    private final JobCategoryService jobCategoryService;

    public RecruitmentRequestService(RecruitmentRequestRepository recruitmentRequestRepository,
            JobCategoryService jobCategoryService) {
        this.recruitmentRequestRepository = recruitmentRequestRepository;
        this.jobCategoryService = jobCategoryService;
    }

    @Transactional
    public RecruitmentRequest create(CreateRecruitmentRequestDTO dto) {
        JobCategory category = jobCategoryService.findById(dto.getJobCategoryId());
        RecruitmentRequest rr = new RecruitmentRequest();
        rr.setTitle(dto.getTitle());
        rr.setNumberOfPositions(dto.getNumberOfPositions());
        rr.setPriorityLevel(dto.getPriorityLevel());
        rr.setRequestReason(dto.getRequestReason());
        rr.setJobDescription(dto.getJobDescription());
        rr.setRequirements(dto.getRequirements());
        rr.setPreferredQualifications(dto.getPreferredQualifications());
        rr.setSalaryRangeMin(dto.getSalaryRangeMin());
        rr.setSalaryRangeMax(dto.getSalaryRangeMax());
        rr.setCurrency(dto.getCurrency());
        rr.setEmploymentType(dto.getEmploymentType());
        rr.setWorkLocation(dto.getWorkLocation());
        rr.setExpectedStartDate(dto.getExpectedStartDate());
        rr.setDeadline(dto.getDeadline());
        rr.setStatus(RecruitmentRequestStatus.PENDING_APPROVAL);
        rr.setRequesterId(dto.getRequesterId());
        rr.setJobCategory(category);
        rr.setDepartmentId(dto.getDepartmentId());
        rr.setActive(true);
        return recruitmentRequestRepository.save(rr);
    }

    @Transactional
    public RecruitmentRequest approve(Long id, ApproveRecruitmentRequestDTO dto) {
        RecruitmentRequest rr = this.findById(id);
        rr.setStatus(RecruitmentRequestStatus.APPROVED);
        rr.setApprovedId(dto.getApprovedId());
        rr.setApprovalNotes(dto.getApprovalNotes());
        rr.setApprovedAt(LocalDateTime.now());
        return recruitmentRequestRepository.save(rr);
    }

    public RecruitmentRequest findById(Long id) {
        return recruitmentRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Yêu cầu tuyển dụng không tồn tại"));
    }

    public boolean changeStatus(Long id, RecruitmentRequestStatus status) {
        RecruitmentRequest rr = this.findById(id);
        rr.setStatus(status);
        return recruitmentRequestRepository.save(rr) != null;
    }

    public List<RecruitmentRequest> getAllByDepartmentId(Long departmentId) {
        return recruitmentRequestRepository.findByDepartmentId(departmentId);
    }

    public RecruitmentRequest getById(Long id) {
        return this.findById(id);
    }

    public List<RecruitmentRequest> getAll() {
        return recruitmentRequestRepository.findAllByIsActiveTrue();
    }

}
