package com.example.job_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.job_service.model.JobSkill;

import java.util.List;

@Repository
public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {
    List<JobSkill> findByIsActiveTrue();
}
