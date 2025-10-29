package com.example.job_service.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.job_service.dto.Meta;
import com.example.job_service.dto.PaginationDTO;
import com.example.job_service.model.JobSkill;
import com.example.job_service.repository.JobSkillRepository;

@Service
public class JobSkillService {
    private final JobSkillRepository jobSkillRepository;

    public JobSkillService(JobSkillRepository jobSkillRepository) {
        this.jobSkillRepository = jobSkillRepository;
    }

    public PaginationDTO findByIsActiveTrue(Pageable pageable) {
        Page<JobSkill> pageJobSkill = jobSkillRepository.findByIsActiveTrue(pageable);
        PaginationDTO rs = new PaginationDTO();
        Meta mt = new Meta();
        mt.setPage(pageJobSkill.getNumber() + 1);
        mt.setPageSize(pageJobSkill.getSize());
        mt.setPages(pageJobSkill.getTotalPages());
        mt.setTotal(pageJobSkill.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(pageJobSkill.getContent());
        return rs;
    }

    public JobSkill findById(Long id) {
        return jobSkillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kỹ năng không tồn tại"));
    }

    public PaginationDTO getAllWithFilters(Boolean isActive, String keyword, Pageable pageable) {
        Page<JobSkill> pageJobSkill = jobSkillRepository.findByFilters(isActive, keyword, pageable);
        PaginationDTO rs = new PaginationDTO();
        Meta mt = new Meta();
        mt.setPage(pageJobSkill.getNumber() + 1);
        mt.setPageSize(pageJobSkill.getSize());
        mt.setPages(pageJobSkill.getTotalPages());
        mt.setTotal(pageJobSkill.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(pageJobSkill.getContent());
        return rs;
    }
}
