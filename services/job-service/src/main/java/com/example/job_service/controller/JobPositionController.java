package com.example.job_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.job_service.dto.jobposition.CreateJobPositionDTO;
import com.example.job_service.model.JobPosition;
import com.example.job_service.service.JobPositionService;
import com.example.job_service.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/job-service/job-positions")
public class JobPositionController {
    private final JobPositionService jobPositionService;

    public JobPositionController(JobPositionService jobPositionService) {
        this.jobPositionService = jobPositionService;
    }

    @PostMapping
    @ApiMessage("Tạo vị trí tuyển dụng từ yêu cầu đã duyệt")
    public ResponseEntity<JobPosition> create(@Validated @RequestBody CreateJobPositionDTO dto) {
        return ResponseEntity.ok(jobPositionService.create(dto));
    }

}
