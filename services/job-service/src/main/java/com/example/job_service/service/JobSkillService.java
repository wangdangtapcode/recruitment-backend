package com.example.job_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.job_service.model.JobSkill;
import com.example.job_service.repository.JobSkillRepository;

@Service
public class JobSkillService {
    private final JobSkillRepository jobSkillRepository;

    public JobSkillService(JobSkillRepository jobSkillRepository) {
        this.jobSkillRepository = jobSkillRepository;
    }

    public List<JobSkill> findByIsActiveTrue() {
        return jobSkillRepository.findByIsActiveTrue();
    }

    public JobSkill findById(Long id) {
        return jobSkillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kỹ năng không tồn tại"));
    }
}
