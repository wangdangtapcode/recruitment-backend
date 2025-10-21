package com.example.job_service.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.job_service.dto.jobposition.CreateJobPositionDTO;
import com.example.job_service.model.JobPosition;
import com.example.job_service.model.JobPositionSkill;
import com.example.job_service.model.JobSkill;
import com.example.job_service.model.RecruitmentRequest;
import com.example.job_service.repository.JobPositionRepository;
import com.example.job_service.utils.enums.JobPositionStatus;
import com.example.job_service.utils.enums.RecruitmentRequestStatus;

@Service
public class JobPositionService {
    private final JobPositionRepository jobPositionRepository;
    private final RecruitmentRequestService recruitmentRequestService;
    private final JobSkillService jobSkillService;
    private final JobPositionSkillService jobPositionSkillService;

    public JobPositionService(JobPositionRepository jobPositionRepository,
            RecruitmentRequestService recruitmentRequestService, JobSkillService jobSkillService,
            JobPositionSkillService jobPositionSkillService) {
        this.jobPositionRepository = jobPositionRepository;
        this.recruitmentRequestService = recruitmentRequestService;
        this.jobSkillService = jobSkillService;
        this.jobPositionSkillService = jobPositionSkillService;
    }

    @Transactional
    public JobPosition create(CreateJobPositionDTO dto) {
        RecruitmentRequest rr = recruitmentRequestService.findById(dto.getRecruitmentRequestId());

        JobPosition position = new JobPosition();
        position.setTitle(dto.getTitle());
        position.setDescription(dto.getDescription());
        position.setResponsibilities(dto.getResponsibilities());
        position.setRequirements(dto.getRequirements());
        position.setPreferredQualifications(dto.getPreferredQualifications());
        position.setBenefits(dto.getBenefits());
        position.setSalaryRangeMin(dto.getSalaryRangeMin());
        position.setSalaryRangeMax(dto.getSalaryRangeMax());
        position.setCurrency(dto.getCurrency());
        position.setEmploymentType(dto.getEmploymentType());
        position.setExperienceLevel(dto.getExperienceLevel());
        position.setWorkLocation(dto.getWorkLocation());
        position.setRemoteWorkAllowed(dto.isRemoteWorkAllowed());
        position.setNumberOfOpenings(dto.getNumberOfOpenings());
        position.setApplicationDeadline(dto.getApplicationDeadline());
        position.setStatus(JobPositionStatus.DRAFT);
        position.setRecruitmentRequest(rr);
        position = jobPositionRepository.save(position);

        if (dto.getSkills() != null && !dto.getSkills().isEmpty()) {
            List<JobPositionSkill> links = new ArrayList<>();
            for (CreateJobPositionDTO.SkillRequirement s : dto.getSkills()) {
                JobSkill skill = jobSkillService.findById(s.getSkillId());
                JobPositionSkill link = new JobPositionSkill();
                link.setPosition(position);
                link.setSkill(skill);
                link.setProficiencyLevel(s.getProficiencyLevel());
                link.setRequired(s.isRequired());
                links.add(link);
            }
            jobPositionSkillService.saveAll(links);
        }
        recruitmentRequestService.changeStatus(rr.getId(), RecruitmentRequestStatus.IN_PROGRESS);
        return position;
    }
}
