package com.example.job_service.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.job_service.dto.Meta;
import com.example.job_service.dto.PaginationDTO;
import com.example.job_service.dto.jobposition.CreateJobPositionDTO;
import com.example.job_service.dto.jobposition.GetAllJobPositionDTO;
import com.example.job_service.dto.jobposition.JobPositionResponseDTO;
import com.example.job_service.dto.jobposition.UpdateJobPositionDTO;
import com.example.job_service.exception.IdInvalidException;
import com.example.job_service.model.JobPosition;
import com.example.job_service.model.RecruitmentRequest;
import com.example.job_service.repository.JobPositionRepository;
import com.example.job_service.utils.enums.JobPositionStatus;
import com.example.job_service.utils.enums.RecruitmentRequestStatus;

@Service
public class JobPositionService {
    private final JobPositionRepository jobPositionRepository;
    private final RecruitmentRequestService recruitmentRequestService;
    private final UserService userService;

    public JobPositionService(JobPositionRepository jobPositionRepository,
            RecruitmentRequestService recruitmentRequestService, UserService userService) {
        this.jobPositionRepository = jobPositionRepository;
        this.recruitmentRequestService = recruitmentRequestService;
        this.userService = userService;
    }

    @Transactional
    public JobPosition create(CreateJobPositionDTO dto) throws IdInvalidException {
        RecruitmentRequest rr = recruitmentRequestService.findById(dto.getRecruitmentRequestId());

        JobPosition position = new JobPosition();
        position.setTitle(dto.getTitle());
        position.setDescription(dto.getDescription());
        position.setResponsibilities(dto.getResponsibilities());
        position.setRequirements(dto.getRequirements());
        position.setQualifications(dto.getQualifications());
        position.setBenefits(dto.getBenefits());

        // Fill from RecruitmentRequest if not provided in DTO
        // Chỉ lấy salary từ RecruitmentRequest khi vượt quỹ
        if (rr.isExceedBudget()) {
            position.setSalaryMin(dto.getSalaryMin() != null ? dto.getSalaryMin() : rr.getSalaryMin());
            position.setSalaryMax(dto.getSalaryMax() != null ? dto.getSalaryMax() : rr.getSalaryMax());
            position.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : rr.getCurrency());
        } else {
            // Nếu không vượt quỹ, chỉ sử dụng giá trị từ DTO
            position.setSalaryMin(dto.getSalaryMin());
            position.setSalaryMax(dto.getSalaryMax());
            position.setCurrency(dto.getCurrency());
        }
        position.setEmploymentType(dto.getEmploymentType());
        position.setExperienceLevel(dto.getExperienceLevel());
        position.setLocation(dto.getLocation() != null ? dto.getLocation() : rr.getLocation());
        position.setRemote(dto.getIsRemote() != null ? dto.getIsRemote() : false);
        position.setYearsOfExperience(dto.getYearsOfExperience());

        position.setQuantity(dto.getQuantity());
        position.setDeadline(dto.getDeadline());
        position.setStatus(JobPositionStatus.DRAFT);
        position.setRecruitmentRequest(rr);

        position = jobPositionRepository.save(position);

        recruitmentRequestService.changeStatus(rr.getId(), RecruitmentRequestStatus.COMPLETED);
        return position;
    }

    public JobPosition findById(Long id) throws IdInvalidException {
        return jobPositionRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Vị trí tuyển dụng không tồn tại"));
    }

    public JobPositionResponseDTO getByIdWithDepartmentName(Long id, String token) throws IdInvalidException {
        JobPosition position = this.findById(id);
        JobPositionResponseDTO dto = JobPositionResponseDTO.fromEntity(position);
        Long deptId = position.getRecruitmentRequest().getDepartmentId();
        String deptName = userService.getDepartmentById(deptId, token).getBody().get("name").asText();
        dto.setDepartmentName(deptName);
        return dto;
    }

    public JobPositionResponseDTO getByIdWithPublished(Long id) throws IdInvalidException {
        JobPosition position = this.findById(id);
        if (position.getStatus() != JobPositionStatus.PUBLISHED) {
            throw new IdInvalidException("Vị trí tuyển dụng chưa được xuất bản hoặc không khả dụng");
        }
        JobPositionResponseDTO dto = JobPositionResponseDTO.fromEntity(position);
        Long deptId = position.getRecruitmentRequest().getDepartmentId();
        String deptName = userService.getPublicDepartmentById(deptId).getBody().get("name").asText();
        dto.setDepartmentName(deptName);
        return dto;
    }

    public PaginationDTO findAll(Pageable pageable) {
        Page<JobPosition> pageJobPosition = jobPositionRepository.findAll(pageable);
        PaginationDTO rs = new PaginationDTO();
        Meta mt = new Meta();
        mt.setPage(pageJobPosition.getNumber() + 1);
        mt.setPageSize(pageJobPosition.getSize());
        mt.setPages(pageJobPosition.getTotalPages());
        mt.setTotal(pageJobPosition.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(pageJobPosition.getContent().stream()
                .map(JobPositionResponseDTO::fromEntity)
                .toList());
        return rs;
    }

    public List<JobPosition> findByStatus(JobPositionStatus status) {
        return jobPositionRepository.findByStatus(status);
    }

    public List<JobPosition> findPublished() {
        return jobPositionRepository.findByStatus(JobPositionStatus.PUBLISHED);
    }

    public PaginationDTO findByDepartmentId(Long departmentId, Pageable pageable) {
        Page<JobPosition> pageJobPosition = jobPositionRepository.findByDepartmentId(departmentId, pageable);
        PaginationDTO rs = new PaginationDTO();
        Meta mt = new Meta();
        mt.setPage(pageJobPosition.getNumber() + 1);
        mt.setPageSize(pageJobPosition.getSize());
        mt.setPages(pageJobPosition.getTotalPages());
        mt.setTotal(pageJobPosition.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(pageJobPosition.getContent().stream()
                .map(JobPositionResponseDTO::fromEntity)
                .toList());
        return rs;
    }

    public List<JobPosition> findByDepartmentIdAndStatus(Long departmentId, JobPositionStatus status) {
        return jobPositionRepository.findByDepartmentIdAndStatus(departmentId, status);
    }

    public PaginationDTO findAllWithFilters(Long departmentId, JobPositionStatus status, Long categoryId,
            Boolean published, String keyword, Pageable pageable, String token) {
        Page<JobPosition> pageJobPosition = jobPositionRepository.findByFilters(departmentId, status, categoryId,
                published, keyword, pageable);

        // Get unique department IDs
        List<Long> departmentIds = pageJobPosition.getContent().stream()
                .map(position -> position.getRecruitmentRequest().getDepartmentId())
                .distinct()
                .toList();

        // Fetch departments by IDs from UserService in one call
        Map<Long, String> departmentNames = userService.getDepartmentsByIds(departmentIds, token);

        PaginationDTO rs = new PaginationDTO();
        Meta mt = new Meta();
        mt.setPage(pageJobPosition.getNumber() + 1);
        mt.setPageSize(pageJobPosition.getSize());
        mt.setPages(pageJobPosition.getTotalPages());
        mt.setTotal(pageJobPosition.getTotalElements());
        rs.setMeta(mt);

        // Convert to JobPositionResponseDTO with department names
        List<JobPositionResponseDTO> result = pageJobPosition.getContent().stream()
                .map(position -> {
                    JobPositionResponseDTO dto = JobPositionResponseDTO.fromEntity(position);
                    Long deptId = position.getRecruitmentRequest().getDepartmentId();
                    dto.setDepartmentName(departmentNames.getOrDefault(deptId, "Unknown Department"));
                    return dto;
                })
                .toList();

        rs.setResult(result);
        return rs;
    }

    public PaginationDTO findAllWithFiltersSimplified(Long departmentId, JobPositionStatus status, Long categoryId,
            Boolean published, String keyword, Pageable pageable, String token) {
        Page<JobPosition> pageJobPosition = jobPositionRepository.findByFilters(departmentId, status, categoryId,
                published, keyword, pageable);

        // Get unique department IDs
        List<Long> departmentIds = pageJobPosition.getContent().stream()
                .map(position -> position.getRecruitmentRequest().getDepartmentId())
                .distinct()
                .toList();

        // Fetch departments by IDs from UserService in one call
        Map<Long, String> departmentNames = userService.getDepartmentsByIds(departmentIds, token);

        PaginationDTO rs = new PaginationDTO();
        Meta mt = new Meta();
        mt.setPage(pageJobPosition.getNumber() + 1);
        mt.setPageSize(pageJobPosition.getSize());
        mt.setPages(pageJobPosition.getTotalPages());
        mt.setTotal(pageJobPosition.getTotalElements());
        rs.setMeta(mt);

        // Convert to GetAllJobPositionDTO with department names
        List<GetAllJobPositionDTO> result = pageJobPosition.getContent().stream()
                .map(position -> {
                    GetAllJobPositionDTO dto = GetAllJobPositionDTO.fromEntity(position);
                    Long deptId = position.getRecruitmentRequest().getDepartmentId();
                    dto.setDepartmentName(departmentNames.getOrDefault(deptId, "Unknown Department"));
                    return dto;
                })
                .toList();

        rs.setResult(result);
        return rs;
    }

    @Transactional
    public JobPosition update(Long id, UpdateJobPositionDTO dto) throws IdInvalidException {
        JobPosition position = this.findById(id);

        if (dto.getTitle() != null) {
            position.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            position.setDescription(dto.getDescription());
        }
        if (dto.getResponsibilities() != null) {
            position.setResponsibilities(dto.getResponsibilities());
        }
        if (dto.getRequirements() != null) {
            position.setRequirements(dto.getRequirements());
        }
        if (dto.getQualifications() != null) {
            position.setQualifications(dto.getQualifications());
        }
        if (dto.getBenefits() != null) {
            position.setBenefits(dto.getBenefits());
        }
        if (dto.getSalaryMin() != null) {
            position.setSalaryMin(dto.getSalaryMin());
        }
        if (dto.getSalaryMax() != null) {
            position.setSalaryMax(dto.getSalaryMax());
        }
        if (dto.getCurrency() != null) {
            position.setCurrency(dto.getCurrency());
        }
        if (dto.getEmploymentType() != null) {
            position.setEmploymentType(dto.getEmploymentType());
        }
        if (dto.getExperienceLevel() != null) {
            position.setExperienceLevel(dto.getExperienceLevel());
        }
        if (dto.getLocation() != null) {
            position.setLocation(dto.getLocation());
        }
        if (dto.getIsRemote() != null) {
            position.setRemote(dto.getIsRemote());
        }
        if (dto.getQuantity() != null) {
            position.setQuantity(dto.getQuantity());
        }

        if (dto.getDeadline() != null) {
            position.setDeadline(dto.getDeadline());
        }
        if (dto.getYearsOfExperience() != null) {
            position.setYearsOfExperience(dto.getYearsOfExperience());
        }

        return jobPositionRepository.save(position);
    }

    @Transactional
    public boolean delete(Long id) throws IdInvalidException {
        JobPosition position = this.findById(id);
        jobPositionRepository.delete(position);
        return true;
    }

    @Transactional
    public JobPosition publish(Long id) throws IdInvalidException {
        JobPosition position = this.findById(id);
        if (position.getStatus() != JobPositionStatus.DRAFT) {
            throw new IdInvalidException("Chỉ có thể publish vị trí ở trạng thái DRAFT");
        }
        position.setStatus(JobPositionStatus.PUBLISHED);
        return jobPositionRepository.save(position);
    }

    @Transactional
    public JobPosition close(Long id) throws IdInvalidException {
        JobPosition position = this.findById(id);
        if (position.getStatus() != JobPositionStatus.PUBLISHED) {
            throw new IdInvalidException("Chỉ có thể đóng vị trí ở trạng thái PUBLISHED");
        }
        position.setStatus(JobPositionStatus.CLOSED);
        return jobPositionRepository.save(position);
    }

    @Transactional
    public JobPosition reopen(Long id) throws IdInvalidException {
        JobPosition position = this.findById(id);
        if (position.getStatus() != JobPositionStatus.CLOSED) {
            throw new IdInvalidException("Chỉ có thể mở lại vị trí ở trạng thái CLOSED");
        }
        position.setStatus(JobPositionStatus.PUBLISHED);
        return jobPositionRepository.save(position);
    }

    @Transactional
    public void incrementApplicationCount(Long jobPositionId) throws IdInvalidException {
        JobPosition position = this.findById(jobPositionId);
        int current = position.getApplicationCount();
        position.setApplicationCount(current + 1);
        jobPositionRepository.save(position);
    }
}
