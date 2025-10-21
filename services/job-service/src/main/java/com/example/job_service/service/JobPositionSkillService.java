package com.example.job_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.job_service.model.JobPositionSkill;
import com.example.job_service.repository.JobPositionSkillRepository;

import jakarta.transaction.Transactional;

@Service
public class JobPositionSkillService {
    private final JobPositionSkillRepository jobPositionSkillRepository;

    public JobPositionSkillService(JobPositionSkillRepository jobPositionSkillRepository) {
        this.jobPositionSkillRepository = jobPositionSkillRepository;
    }

    @Transactional
    public List<JobPositionSkill> saveAll(List<JobPositionSkill> jobPositionSkills) {
        return jobPositionSkillRepository.saveAll(jobPositionSkills);
    }
}
