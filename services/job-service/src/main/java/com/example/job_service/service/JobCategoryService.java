package com.example.job_service.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.job_service.dto.Meta;
import com.example.job_service.dto.PaginationDTO;
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

    public PaginationDTO getAllWithFilters(Long departmentId, String keyword, Pageable pageable) {
        Page<JobCategory> pageJobCategory = jobCategoryRepository.findByFilters(departmentId, keyword, pageable);
        PaginationDTO rs = new PaginationDTO();
        Meta mt = new Meta();
        mt.setPage(pageJobCategory.getNumber() + 1);
        mt.setPageSize(pageJobCategory.getSize());
        mt.setPages(pageJobCategory.getTotalPages());
        mt.setTotal(pageJobCategory.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(pageJobCategory.getContent());
        return rs;
    }
}
