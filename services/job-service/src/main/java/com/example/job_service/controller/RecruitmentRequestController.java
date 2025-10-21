package com.example.job_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.job_service.dto.recruitment.ApproveRecruitmentRequestDTO;
import com.example.job_service.dto.recruitment.CreateRecruitmentRequestDTO;
import com.example.job_service.model.RecruitmentRequest;
import com.example.job_service.service.RecruitmentRequestService;
import com.example.job_service.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/job-service/recruitment-requests")
public class RecruitmentRequestController {
    private final RecruitmentRequestService recruitmentRequestService;

    public RecruitmentRequestController(RecruitmentRequestService recruitmentRequestService) {
        this.recruitmentRequestService = recruitmentRequestService;
    }

    @PostMapping
    @ApiMessage("Tạo yêu cầu tuyển dụng")
    public ResponseEntity<RecruitmentRequest> create(@Validated @RequestBody CreateRecruitmentRequestDTO dto) {
        return ResponseEntity.ok(recruitmentRequestService.create(dto));
    }

    @PostMapping("/{id}/approve")
    @ApiMessage("Duyệt yêu cầu tuyển dụng")
    public ResponseEntity<RecruitmentRequest> approve(@PathVariable Long id,
            @Validated @RequestBody ApproveRecruitmentRequestDTO dto) {
        return ResponseEntity.ok(recruitmentRequestService.approve(id, dto));
    }

    @GetMapping("/department/{departmentId}")
    @ApiMessage("Lấy danh sách yêu cầu tuyển dụng")
    public ResponseEntity<List<RecruitmentRequest>> getAllByDepartmentId(@PathVariable Long departmentId) {
        return ResponseEntity.ok(recruitmentRequestService.getAllByDepartmentId(departmentId));
    }

    @GetMapping("/{id}")
    @ApiMessage("Lấy yêu cầu tuyển dụng theo id")
    public ResponseEntity<RecruitmentRequest> getById(@PathVariable Long id) {
        return ResponseEntity.ok(recruitmentRequestService.getById(id));
    }

    @GetMapping
    @ApiMessage("Lấy danh sách tất cả yêu cầu tuyển dụng")
    public ResponseEntity<List<RecruitmentRequest>> getAll() {
        return ResponseEntity.ok(recruitmentRequestService.getAll());
    }
}
