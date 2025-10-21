package com.example.job_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.job_service.model.JobCategory;
import com.example.job_service.repository.JobCategoryRepository;

@Service
public class JobCategoryService {
    private final JobCategoryRepository jobCategoryRepository;

    public JobCategoryService(JobCategoryRepository jobCategoryRepository) {
        this.jobCategoryRepository = jobCategoryRepository;
    }

    public List<JobCategory> getAllJobCategoriesByDepartmentId(Long departmentId) {
        return jobCategoryRepository.findByDepartmentIdAndIsActiveTrue(departmentId);
    }

    public JobCategory findById(Long id) {
        return jobCategoryRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Danh mục công việc không tồn tại"));
    }
}
