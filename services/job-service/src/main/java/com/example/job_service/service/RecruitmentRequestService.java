package com.example.job_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.job_service.dto.Meta;
import com.example.job_service.dto.PaginationDTO;
import com.example.job_service.dto.recruitment.ApproveRecruitmentRequestDTO;
import com.example.job_service.dto.recruitment.CreateRecruitmentRequestDTO;
import com.example.job_service.dto.recruitment.RecruitmentRequestWithUserDTO;
import com.example.job_service.exception.IdInvalidException;
import com.example.job_service.exception.UserServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.example.job_service.model.JobCategory;
import com.example.job_service.model.RecruitmentRequest;
import com.example.job_service.repository.RecruitmentRequestRepository;
import com.example.job_service.utils.enums.RecruitmentRequestStatus;

@Service
public class RecruitmentRequestService {
    private final RecruitmentRequestRepository recruitmentRequestRepository;
    private final JobCategoryService jobCategoryService;
    private final UserService userService;

    public RecruitmentRequestService(RecruitmentRequestRepository recruitmentRequestRepository,
            JobCategoryService jobCategoryService, UserService userService) {
        this.recruitmentRequestRepository = recruitmentRequestRepository;
        this.jobCategoryService = jobCategoryService;
        this.userService = userService;
    }

    @Transactional
    public RecruitmentRequest create(CreateRecruitmentRequestDTO dto) {
        JobCategory category = jobCategoryService.findById(dto.getJobCategoryId());
        RecruitmentRequest rr = new RecruitmentRequest();
        rr.setTitle(dto.getTitle());
        rr.setNumberOfPositions(dto.getNumberOfPositions());
        rr.setPriorityLevel(dto.getPriorityLevel());
        rr.setReason(dto.getReason());
        rr.setDescription(dto.getDescription());
        rr.setRequirements(dto.getRequirements());
        rr.setBenefits(dto.getBenefits());
        // Chỉ set salary khi vượt quỹ
        if (dto.isExceedBudget()) {
            rr.setSalaryMin(dto.getSalaryMin());
            rr.setSalaryMax(dto.getSalaryMax());
            rr.setCurrency(dto.getCurrency());
        }

        rr.setLocation(dto.getLocation());
        rr.setExceedBudget(dto.isExceedBudget());
        rr.setStatus(RecruitmentRequestStatus.PENDING);
        rr.setRequesterId(dto.getRequesterId());
        rr.setJobCategory(category);
        rr.setDepartmentId(dto.getDepartmentId());
        rr.setActive(true);
        return recruitmentRequestRepository.save(rr);
    }

    @Transactional
    public RecruitmentRequest approve(Long id, ApproveRecruitmentRequestDTO dto, Long approvedId)
            throws IdInvalidException {
        RecruitmentRequest rr = this.findById(id);
        rr.setStatus(RecruitmentRequestStatus.APPROVED);
        rr.setApprovedId(approvedId); // Lưu employeeId
        rr.setApprovalNotes(dto.getApprovalNotes());
        rr.setApprovedAt(LocalDateTime.now());
        return recruitmentRequestRepository.save(rr);
    }

    public RecruitmentRequest findById(Long id) throws IdInvalidException {
        return recruitmentRequestRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Yêu cầu tuyển dụng không tồn tại"));
    }

    public boolean changeStatus(Long id, RecruitmentRequestStatus status) throws IdInvalidException {
        RecruitmentRequest rr = this.findById(id);
        rr.setStatus(status);
        return recruitmentRequestRepository.save(rr) != null;
    }

    public List<RecruitmentRequest> getAllByDepartmentId(Long departmentId) {
        return recruitmentRequestRepository.findByDepartmentId(departmentId);
    }

    public RecruitmentRequest getById(Long id) throws IdInvalidException {
        return recruitmentRequestRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Yêu cầu tuyển dụng không tồn tại"));
    }

    public List<RecruitmentRequest> getAll() {
        return recruitmentRequestRepository.findAll().stream()
                .filter(request -> request.isActive())
                .collect(Collectors.toList());
    }

    // Methods with user information
    public RecruitmentRequestWithUserDTO getByIdWithUser(Long id, String token) throws IdInvalidException {
        RecruitmentRequest request = this.findById(id);
        return convertToWithUserDTO(request, token);
    }

    public PaginationDTO getAllByDepartmentIdWithUser(Long departmentId, String token, Pageable pageable) {
        Page<RecruitmentRequest> requests = recruitmentRequestRepository.findByDepartmentIdAndIsActiveTrue(departmentId,
                pageable);
        return convertToWithUserDTOList(requests, token);
    }

    public PaginationDTO getAllWithUser(String token, Pageable pageable) {

        Page<RecruitmentRequest> requests = recruitmentRequestRepository.findAllByIsActiveTrue(pageable);
        return convertToWithUserDTOList(requests, token);
    }

    public PaginationDTO getAllWithFilters(Long departmentId, String status, Long createdBy, String keyword,
            String token,
            Pageable pageable) {
        Page<RecruitmentRequest> requests = recruitmentRequestRepository.findByFilters(departmentId, status, createdBy,
                keyword,
                pageable);
        return convertToWithUserDTOList(requests, token);
    }

    private RecruitmentRequestWithUserDTO convertToWithUserDTO(RecruitmentRequest request, String token) {
        RecruitmentRequestWithUserDTO dto = RecruitmentRequestWithUserDTO.fromEntity(request);

        if (request.getRequesterId() != null) {
            // Lấy thông tin employee (requesterId lưu employeeId)
            ResponseEntity<JsonNode> requesterResponse = userService.getEmployeeById(request.getRequesterId(), token);
            if (requesterResponse.getStatusCode().is2xxSuccessful()) {
                dto.setRequester(requesterResponse.getBody());
            } else {
                // Nếu lỗi → ném lại exception có nội dung JSON lỗi
                throw new UserServiceException(requesterResponse);
            }
        }

        if (request.getApprovedId() != null) {
            // Lấy thông tin employee (approvedId lưu employeeId)
            ResponseEntity<JsonNode> approverResponse = userService.getEmployeeById(request.getApprovedId(), token);
            if (approverResponse.getStatusCode().is2xxSuccessful()) {
                dto.setApprover(approverResponse.getBody());
            } else {
                throw new UserServiceException(approverResponse);
            }
        }

        if (request.getDepartmentId() != null) {
            ResponseEntity<JsonNode> departmentResponse = userService.getDepartmentById(request.getDepartmentId(),
                    token);
            if (departmentResponse.getStatusCode().is2xxSuccessful()) {
                dto.setDepartment(departmentResponse.getBody());
            } else {
                throw new UserServiceException(departmentResponse);
            }
        }
        return dto;
    }

    private PaginationDTO convertToWithUserDTOList(Page<RecruitmentRequest> requests,
            String token) {
        PaginationDTO rs = new PaginationDTO();
        Meta mt = new Meta();
        mt.setPage(requests.getNumber() + 1);
        mt.setPageSize(requests.getSize());
        mt.setPages(requests.getTotalPages());
        mt.setTotal(requests.getTotalElements());
        rs.setMeta(mt);

        // Lấy tất cả employee IDs cần thiết (requesterId và approvedId lưu employeeId)
        List<Long> employeeIds = requests.getContent().stream()
                .flatMap(request -> {
                    if (request.getRequesterId() != null && request.getApprovedId() != null) {
                        return List.of(request.getRequesterId(), request.getApprovedId()).stream();
                    } else if (request.getRequesterId() != null) {
                        return List.of(request.getRequesterId()).stream();
                    } else if (request.getApprovedId() != null) {
                        return List.of(request.getApprovedId()).stream();
                    }
                    return List.<Long>of().stream();
                })
                .distinct()
                .collect(Collectors.toList());

        // Lấy tất cả department IDs cần thiết
        List<Long> departmentIds = requests.getContent().stream()
                .map(RecruitmentRequest::getDepartmentId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        // Lấy thông tin tất cả employees một lần
        Map<Long, JsonNode> employeeMap = userService.getEmployeesByIds(employeeIds, token);

        // Lấy thông tin tất cả departments một lần
        Map<Long, JsonNode> departmentMap = departmentIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            try {
                                ResponseEntity<JsonNode> response = userService.getDepartmentById(id, token);
                                return response.getBody();
                            } catch (Exception e) {
                                return null;
                            }
                        }));

        // Convert từng request sử dụng cached data
        rs.setResult(requests.getContent().stream()
                .map(request -> {
                    RecruitmentRequestWithUserDTO dto = RecruitmentRequestWithUserDTO.fromEntity(request);

                    // Set requester info (requesterId lưu employeeId)
                    if (request.getRequesterId() != null) {
                        dto.setRequester(employeeMap.get(request.getRequesterId()));
                    }

                    // Set approver info (approvedId lưu employeeId)
                    if (request.getApprovedId() != null) {
                        dto.setApprover(employeeMap.get(request.getApprovedId()));
                    }

                    // Set department info
                    if (request.getDepartmentId() != null) {
                        dto.setDepartment(departmentMap.get(request.getDepartmentId()));
                    }
                    return dto;
                })
                .toList());
        return rs;
    }

    @Transactional
    public RecruitmentRequest update(Long id, CreateRecruitmentRequestDTO dto) throws IdInvalidException {
        RecruitmentRequest rr = this.findById(id);
        JobCategory category = jobCategoryService.findById(dto.getJobCategoryId());

        rr.setTitle(dto.getTitle());
        rr.setNumberOfPositions(dto.getNumberOfPositions());
        rr.setPriorityLevel(dto.getPriorityLevel());
        rr.setReason(dto.getReason());
        rr.setDescription(dto.getDescription());
        rr.setRequirements(dto.getRequirements());

        // Chỉ set salary khi vượt quỹ
        if (dto.isExceedBudget()) {
            rr.setSalaryMin(dto.getSalaryMin());
            rr.setSalaryMax(dto.getSalaryMax());
            rr.setCurrency(dto.getCurrency());
        }

        rr.setLocation(dto.getLocation());
        rr.setExceedBudget(dto.isExceedBudget());
        rr.setJobCategory(category);
        rr.setDepartmentId(dto.getDepartmentId());

        return recruitmentRequestRepository.save(rr);
    }

    @Transactional
    public boolean delete(Long id) throws IdInvalidException {
        RecruitmentRequest rr = this.findById(id);
        rr.setActive(false);
        recruitmentRequestRepository.save(rr);
        return true;
    }

    @Transactional
    public RecruitmentRequest reject(Long id, String reason) throws IdInvalidException {
        RecruitmentRequest rr = this.findById(id);
        rr.setStatus(RecruitmentRequestStatus.REJECTED);
        rr.setApprovalNotes(reason);
        rr.setApprovedAt(LocalDateTime.now());
        return recruitmentRequestRepository.save(rr);
    }

}
