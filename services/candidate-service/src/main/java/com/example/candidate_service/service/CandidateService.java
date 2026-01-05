package com.example.candidate_service.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.candidate_service.repository.CandidateRepository;
import com.example.candidate_service.dto.Meta;
import com.example.candidate_service.dto.PaginationDTO;
import com.example.candidate_service.dto.candidate.CandidateGetAllResponseDTO;
import com.example.candidate_service.dto.candidate.CandidateStatisticsDTO;
import com.example.candidate_service.dto.candidate.CandidateDetailResponseDTO;
import com.example.candidate_service.dto.candidate.CreateCandidateDTO;
import com.example.candidate_service.dto.candidate.UpdateCandidateDTO;
import com.example.candidate_service.dto.candidate.UploadCVDTO;
import com.example.candidate_service.exception.IdInvalidException;
import com.example.candidate_service.model.Candidate;
import com.example.candidate_service.utils.enums.CandidateStatus;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class CandidateService {
    private final CandidateRepository candidateRepository;
    private final JobService jobService;
    private final ScheduleService communicationService;
    private final CommentService commentService;
    private final ReviewService reviewService;

    public CandidateService(CandidateRepository candidateRepository, JobService jobService,
            ScheduleService communicationService, CommentService commentService, ReviewService reviewService) {
        this.candidateRepository = candidateRepository;
        this.jobService = jobService;
        this.communicationService = communicationService;
        this.commentService = commentService;
        this.reviewService = reviewService;
    }

    public boolean existsByEmail(String email) {
        return candidateRepository.existsByEmail(email);
    }

    public Candidate findByEmail(String email) throws IdInvalidException {
        return candidateRepository.findByEmail(email)
                .orElseThrow(() -> new IdInvalidException("ứng viên không tồn tại"));
    }

    public Candidate saveCandidate(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    public long countCandidatesByJobPositionId(Long jobPositionId) {
        return candidateRepository.countByJobPositionId(jobPositionId);
    }

    public PaginationDTO getAllWithFilters(Long candidateId, Long jobPositionId, CandidateStatus status,
            String startDate, String endDate, String keyword, Long departmentId, Pageable pageable, String token) {
        Page<Candidate> candidates;

        // Parse date strings to LocalDate
        LocalDate parsedStartDate = null;
        LocalDate parsedEndDate = null;

        if (startDate != null && !startDate.isEmpty()) {
            try {
                parsedStartDate = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
            } catch (Exception e) {
                System.err.println("Error parsing startDate: " + e.getMessage());
            }
        }

        if (endDate != null && !endDate.isEmpty()) {
            try {
                parsedEndDate = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
            } catch (Exception e) {
                System.err.println("Error parsing endDate: " + e.getMessage());
            }
        }

        // Lấy jobPositionIds từ departmentId nếu có và cache job positions info
        List<Long> jobPositionIds = null;
        Map<Long, JsonNode> cachedJobPositions = new HashMap<>();

        if (departmentId != null) {
            // Lấy job positions với đầy đủ thông tin từ departmentId
            Map<Long, JsonNode> deptJobPositions = jobService.getJobPositionsByDepartmentId(departmentId, token);
            jobPositionIds = new ArrayList<>(deptJobPositions.keySet());

            // Cache job positions info để không cần gọi lại
            cachedJobPositions.putAll(deptJobPositions);

            // If no job positions found for this department, return empty result
            if (jobPositionIds.isEmpty()) {
                PaginationDTO paginationDTO = new PaginationDTO();
                Meta meta = new Meta();
                meta.setPage(pageable.getPageNumber() + 1);
                meta.setPageSize(pageable.getPageSize());
                meta.setPages(0);
                meta.setTotal(0);
                paginationDTO.setMeta(meta);
                paginationDTO.setResult(List.of());
                return paginationDTO;
            }
        }

        // Gọi một query duy nhất hỗ trợ tất cả các filter
        candidates = candidateRepository.findByFilters(
                jobPositionId, status, candidateId,
                parsedStartDate, parsedEndDate,
                keyword, jobPositionIds,
                pageable);

        PaginationDTO paginationDTO = new PaginationDTO();
        Meta meta = new Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(candidates.getTotalPages());
        meta.setTotal(candidates.getTotalElements());
        paginationDTO.setMeta(meta);

        // Batch collect unique jobPositionIds để enrich với job position info
        Set<Long> uniqueJobIds = candidates.getContent().stream()
                .map(Candidate::getJobPositionId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> idToTitle = new HashMap<>();
        Map<Long, Long> idToDepartmentId = new HashMap<>();

        if (!uniqueJobIds.isEmpty() && token != null && !token.isEmpty()) {
            // Lấy các job position IDs chưa có trong cache
            List<Long> missingJobIds = uniqueJobIds.stream()
                    .filter(id -> !cachedJobPositions.containsKey(id))
                    .collect(Collectors.toList());

            // Chỉ gọi API cho các job positions chưa có trong cache - sử dụng API /simple
            if (!missingJobIds.isEmpty()) {
                Map<Long, JsonNode> missingJobPositions = jobService.getJobPositionsByIdsSimple(missingJobIds, token);
                cachedJobPositions.putAll(missingJobPositions);
            }

            // Extract info từ cached job positions
            for (Long jpId : uniqueJobIds) {
                JsonNode body = cachedJobPositions.get(jpId);
                if (body != null) {
                    if (body.has("title")) {
                        idToTitle.put(jpId, body.get("title").asText());
                    }
                    // Lấy departmentId từ recruitmentRequest.departmentId
                    // API /simple trả về JobPosition entity với recruitmentRequest được serialize
                    if (body.has("recruitmentRequest") && !body.get("recruitmentRequest").isNull()) {
                        JsonNode rr = body.get("recruitmentRequest");
                        if (rr != null && rr.has("departmentId") && !rr.get("departmentId").isNull()) {
                            idToDepartmentId.put(jpId, rr.get("departmentId").asLong());
                        }
                    }
                }
            }
        }

        paginationDTO.setResult(candidates.getContent().stream()
                .map(candidate -> {
                    CandidateGetAllResponseDTO dto = CandidateGetAllResponseDTO.fromEntity(candidate);
                    if (dto.getJobPositionId() != null) {
                        dto.setJobPositionTitle(idToTitle.get(dto.getJobPositionId()));
                        dto.setDepartmentId(idToDepartmentId.get(dto.getJobPositionId()));
                    }
                    return dto;
                })
                .toList());

        return paginationDTO;
    }

    public CandidateDetailResponseDTO getById(Long id) throws IdInvalidException {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("ứng viên không tồn tại"));
        return CandidateDetailResponseDTO.fromEntity(candidate);
    }

    @Transactional
    public Candidate create(CreateCandidateDTO dto) throws IdInvalidException {
        // Kiểm tra ứng viên đã nộp hồ sơ cho vị trí này chưa
        boolean existsCandidate = candidateRepository.existsByEmailAndJobPositionId(dto.getEmail(),
                dto.getJobPositionId());
        if (existsCandidate) {
            throw new IdInvalidException("Ứng viên đã nộp hồ sơ cho vị trí này");
        }

        Candidate candidate = new Candidate();
        candidate.setName(dto.getName());
        candidate.setEmail(dto.getEmail());
        candidate.setPhone(dto.getPhone());
        candidate.setJobPositionId(dto.getJobPositionId());
        candidate.setResumeUrl(dto.getCvUrl());
        candidate.setNotes(dto.getNotes());
        candidate.setAppliedDate(LocalDate.now());
        candidate.setStatus(CandidateStatus.SUBMITTED);

        // Set createdBy nếu có, nếu không sẽ được set tự động bởi @PrePersist
        if (dto.getCreatedBy() != null) {
            candidate.setCreatedBy(dto.getCreatedBy());
        }

        return candidateRepository.save(candidate);
    }

    @Transactional
    public CandidateDetailResponseDTO update(Long id, UpdateCandidateDTO dto) throws IdInvalidException {
        Candidate existing = candidateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("ứng viên không tồn tại"));

        Optional.ofNullable(dto.getName()).ifPresent(existing::setName);
        Optional.ofNullable(dto.getEmail()).ifPresent(existing::setEmail);
        Optional.ofNullable(dto.getPhone()).ifPresent(existing::setPhone);
        Optional.ofNullable(dto.getDateOfBirth()).ifPresent(existing::setDateOfBirth);
        Optional.ofNullable(dto.getGender()).ifPresent(existing::setGender);
        Optional.ofNullable(dto.getNationality()).ifPresent(existing::setNationality);
        Optional.ofNullable(dto.getIdNumber()).ifPresent(existing::setIdNumber);
        Optional.ofNullable(dto.getAddress()).ifPresent(existing::setAddress);
        Optional.ofNullable(dto.getAvatarUrl()).ifPresent(existing::setAvatarUrl);
        Optional.ofNullable(dto.getHighestEducation()).ifPresent(existing::setHighestEducation);
        Optional.ofNullable(dto.getUniversity()).ifPresent(existing::setUniversity);
        Optional.ofNullable(dto.getGraduationYear()).ifPresent(existing::setGraduationYear);
        Optional.ofNullable(dto.getGpa()).ifPresent(existing::setGpa);
        Optional.ofNullable(dto.getNotes()).ifPresent(existing::setNotes);
        // Stage removed, using status instead

        Candidate saved = candidateRepository.save(existing);
        return CandidateDetailResponseDTO.fromEntity(saved);
    }

    @Transactional
    public void delete(Long id) throws IdInvalidException {
        Candidate existing = candidateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("ứng viên không tồn tại"));
        candidateRepository.delete(existing);
    }

    public List<CandidateDetailResponseDTO> getByIds(List<Long> ids) {
        return candidateRepository.findAllById(ids).stream().map(CandidateDetailResponseDTO::fromEntity).toList();
    }

    @Transactional
    public Candidate createCandidateFromApplication(UploadCVDTO dto)
            throws IOException, IdInvalidException {

        boolean existsCandidate = candidateRepository.existsByEmailAndJobPositionId(dto.getEmail(),
                dto.getJobPositionId());
        if (existsCandidate) {
            throw new IdInvalidException("Ứng viên đã nộp hồ sơ cho vị trí này");
        }
        Candidate candidate = new Candidate();
        candidate.setEmail(dto.getEmail());
        candidate.setName(dto.getName());
        candidate.setPhone(dto.getPhone());

        candidate.setJobPositionId(dto.getJobPositionId());
        candidate.setAppliedDate(LocalDate.now());
        candidate.setStatus(CandidateStatus.SUBMITTED);
        candidate.setResumeUrl(dto.getCvUrl());
        if (dto.getNotes() != null) {
            candidate.setNotes(dto.getNotes());
        }

        return candidateRepository.save(candidate);
    }

    @Transactional
    public CandidateDetailResponseDTO updateCandidateStatus(Long id, String status, String feedback, Long updatedBy)
            throws IdInvalidException {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Ứng viên không tồn tại"));

        CandidateStatus newStatus = CandidateStatus.valueOf(status);
        candidate.setStatus(newStatus);
        candidate.setUpdatedBy(updatedBy);

        if (newStatus == CandidateStatus.REJECTED) {
            if (feedback != null) {
                candidate.setRejectionReason(feedback);
            }
        }
        // Feedback field removed from Candidate model
        Candidate savedCandidate = candidateRepository.save(candidate);
        return CandidateDetailResponseDTO.fromEntity(savedCandidate);
    }

    public CandidateDetailResponseDTO getCandidateDetailById(Long id, String token) throws IdInvalidException {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Ứng viên không tồn tại"));
        CandidateDetailResponseDTO dto = CandidateDetailResponseDTO.fromEntity(candidate);

        // Comments
        if (candidate.getComments() != null) {
            dto.setComments(commentService.getByCandidateId(candidate.getId(), token));
        }
        // Reviews
        try {
            dto.setReviews(reviewService.getByCandidateId(candidate.getId(), token));
        } catch (Exception e) {
            dto.setReviews(new ArrayList<>());
        }
        // JobPosition from job service
        if (candidate.getJobPositionId() != null) {
            dto.setJobPosition(jobService.getJobPositionById(candidate.getJobPositionId(), token).getBody());
        }
        // Upcoming schedules for this candidate
        if (candidate.getId() != null) {
            var res = communicationService.getUpcomingSchedulesForCandidate(candidate.getId(), token);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                if (res.getBody().isArray()) {
                    List<Object> list = new ArrayList<>();
                    res.getBody().forEach(list::add);
                    dto.setUpcomingSchedules(list);
                } else if (res.getBody().has("data")) {
                    List<Object> list = new ArrayList<>();
                    res.getBody().get("data").forEach(list::add);
                    dto.setUpcomingSchedules(list);
                }
            }
        }
        return dto;
    }

    public List<CandidateStatisticsDTO> getCandidatesForStatistics(CandidateStatus status,
            String startDate, String endDate, Long jobPositionId, Long departmentId, String token) {
        // Parse date strings to LocalDate
        LocalDate parsedStartDate = null;
        LocalDate parsedEndDate = null;

        if (startDate != null && !startDate.isEmpty()) {
            try {
                parsedStartDate = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
            } catch (Exception e) {
                System.err.println("Error parsing startDate: " + e.getMessage());
            }
        }

        if (endDate != null && !endDate.isEmpty()) {
            try {
                parsedEndDate = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
            } catch (Exception e) {
                System.err.println("Error parsing endDate: " + e.getMessage());
            }
        }

        // Lấy tất cả candidates với filters
        Pageable pageable = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "appliedDate"));
        Page<Candidate> candidates = candidateRepository.findByFilters(
                jobPositionId, status, null, parsedStartDate, parsedEndDate, null, null, pageable);
        // Batch collect unique jobPositionIds để lấy departmentId
        Set<Long> uniqueJobIds = candidates.getContent().stream()
                .map(Candidate::getJobPositionId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Long, Long> idToDepartmentId = new HashMap<>();
        if (!uniqueJobIds.isEmpty() && token != null && !token.isEmpty()) {
            // Gọi API một lần để lấy tất cả job positions
            List<Long> jobIdsList = new ArrayList<>(uniqueJobIds);
            Map<Long, JsonNode> jobPositionsMap = jobService.getJobPositionsByIds(jobIdsList, token);

            for (Map.Entry<Long, JsonNode> entry : jobPositionsMap.entrySet()) {
                Long jpId = entry.getKey();
                JsonNode body = entry.getValue();

                // Lấy departmentId từ recruitmentRequest.departmentId
                Long deptId = null;
                if (body.has("recruitmentRequest") && !body.get("recruitmentRequest").isNull()) {
                    JsonNode recruitmentRequest = body.get("recruitmentRequest");
                    if (recruitmentRequest.has("departmentId") && !recruitmentRequest.get("departmentId").isNull()) {
                        deptId = recruitmentRequest.get("departmentId").asLong();
                    }
                }

                if (deptId != null) {
                    idToDepartmentId.put(jpId, deptId);
                }
            }
        }

        // Filter theo departmentId nếu có
        List<Candidate> filteredCandidates = candidates.getContent();
        if (departmentId != null) {
            filteredCandidates = filteredCandidates.stream()
                    .filter(candidate -> {
                        Long candidateDeptId = idToDepartmentId.get(candidate.getJobPositionId());
                        return candidateDeptId != null && candidateDeptId.equals(departmentId);
                    })
                    .collect(Collectors.toList());
        }

        // Convert to statistics DTO
        return filteredCandidates.stream()
                .map(candidate -> {
                    CandidateStatisticsDTO dto = CandidateStatisticsDTO.builder()
                            .id(candidate.getId())
                            .appliedDate(candidate.getAppliedDate())
                            .status(candidate.getStatus())
                            .jobPositionId(candidate.getJobPositionId())
                            .departmentId(idToDepartmentId.get(candidate.getJobPositionId()))
                            .candidateId(candidate.getId())
                            .build();
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
