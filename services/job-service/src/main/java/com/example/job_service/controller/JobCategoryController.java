package com.example.job_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.job_service.model.JobCategory;
import com.example.job_service.service.JobCategoryService;
import com.example.job_service.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/job-service/job-categories")
public class JobCategoryController {
    private final JobCategoryService jobCategoryService;

    public JobCategoryController(JobCategoryService jobCategoryService) {
        this.jobCategoryService = jobCategoryService;
    }

    @GetMapping("/{departmentId}")
    @ApiMessage("Lấy danh mục theo phòng ban")
    public ResponseEntity<List<JobCategory>> getAllJobCategoriesByDepartmentId(@PathVariable Long departmentId) {
        List<JobCategory> jobCategories = jobCategoryService.getAllJobCategoriesByDepartmentId(departmentId);
        return ResponseEntity.ok(jobCategories);
    }
}
