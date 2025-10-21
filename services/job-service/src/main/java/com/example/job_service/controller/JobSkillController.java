package com.example.job_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.job_service.model.JobSkill;
import com.example.job_service.service.JobSkillService;
import com.example.job_service.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/job-service/job-skills")
public class JobSkillController {
    private final JobSkillService jobSkillService;

    public JobSkillController(JobSkillService jobSkillService) {
        this.jobSkillService = jobSkillService;
    }

    @GetMapping
    @ApiMessage("Lấy danh sách kỹ năng đang hoạt động")
    public ResponseEntity<List<JobSkill>> findByIsActiveTrue() {
        return ResponseEntity.ok(jobSkillService.findByIsActiveTrue());
    }
}
