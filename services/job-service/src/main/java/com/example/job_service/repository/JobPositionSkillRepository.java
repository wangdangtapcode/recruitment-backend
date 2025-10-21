package com.example.job_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.job_service.model.JobPositionSkill;

import java.util.List;

@Repository
public interface JobPositionSkillRepository extends JpaRepository<JobPositionSkill, Long> {
    List<JobPositionSkill> findByPositionId(Long positionId);
}
