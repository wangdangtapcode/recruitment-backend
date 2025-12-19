package com.example.candidate_service.service;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.candidate_service.dto.application.ApplicationResponseDTO;
import com.example.candidate_service.dto.application.UploadCVDTO;
import com.example.candidate_service.dto.application.CreateApplicationDTO;
import com.example.candidate_service.dto.application.UpdateApplicationDTO;
import com.example.candidate_service.dto.PaginationDTO;
import com.example.candidate_service.dto.Meta;
import com.example.candidate_service.exception.IdInvalidException;
import com.example.candidate_service.model.Application;
import com.example.candidate_service.model.Candidate;
import com.example.candidate_service.repository.ApplicationRepository;
import com.example.candidate_service.utils.enums.ApplicationStatus;
import com.example.candidate_service.utils.enums.CandidateStage;
import com.example.candidate_service.messaging.ApplicationEventsProducer;
import com.example.candidate_service.messaging.NotificationProducer;
import com.example.candidate_service.utils.SecurityUtil;
import com.example.candidate_service.dto.application.ApplicationDetailResponseDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CandidateService candidateService;
    private final CloudinaryService cloudinaryService;
    private final ApplicationEventsProducer applicationEventsProducer;
    private final JobService jobService;
    private final CommunicationService communicationService;
    private final CommentService commentService;
    private final ReviewService reviewService;
    private final NotificationProducer notificationProducer;
    private final UserService userService;

    public ApplicationService(ApplicationRepository applicationRepository, CandidateService candidateService,
            CloudinaryService cloudinaryService, ApplicationEventsProducer applicationEventsProducer,
            JobService jobService, CommunicationService communicationService, CommentService commentService,
            ReviewService reviewService, NotificationProducer notificationProducer, UserService userService) {
        this.applicationRepository = applicationRepository;
        this.candidateService = candidateService;
        this.cloudinaryService = cloudinaryService;
        this.applicationEventsProducer = applicationEventsProducer;
        this.jobService = jobService;
        this.communicationService = communicationService;
        this.commentService = commentService;
        this.reviewService = reviewService;
        this.notificationProducer = notificationProducer;
        this.userService = userService;
    }

    public PaginationDTO getAllApplicationsWithFilters(Long candidateId, Long jobPositionId, ApplicationStatus status,
            Pageable pageable, String token) {
        Page<Application> applications = applicationRepository.findByFilters(jobPositionId, status, candidateId,
                pageable);

        PaginationDTO paginationDTO = new PaginationDTO();
        Meta meta = new Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(applications.getTotalPages());
        meta.setTotal(applications.getTotalElements());

        paginationDTO.setMeta(meta);

        // Batch collect unique jobPositionIds
        Set<Long> uniqueJobIds = applications.getContent().stream()
                .map(Application::getJobPositionId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> idToTitle = new HashMap<>();
        Map<Long, Long> idToDepartmentId = new HashMap<>();
        Map<Long, String> idToDepartmentName = new HashMap<>();
        for (Long jpId : uniqueJobIds) {
            try {
                var res = jobService.getJobPositionById(jpId, token);
                if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                    var body = res.getBody();
                    if (body.has("title")) {
                        idToTitle.put(jpId, body.get("title").asText());
                    }
                    if (body.has("departmentId")) {
                        idToDepartmentId.put(jpId, body.get("departmentId").asLong());
                    }
                    if (body.has("departmentName")) {
                        idToDepartmentName.put(jpId, body.get("departmentName").asText());
                    }
                }
            } catch (Exception ignored) {
            }
        }

        paginationDTO.setResult(applications.getContent().stream()
                .map(app -> {
                    ApplicationResponseDTO dto = ApplicationResponseDTO.fromEntity(app);
                    if (dto.getJobPositionId() != null) {
                        dto.setJobPositionTitle(idToTitle.get(dto.getJobPositionId()));
                    }
                    if (dto.getJobPositionId() != null) {
                        dto.setDepartmentId(idToDepartmentId.get(dto.getJobPositionId()));
                    }
                    if (dto.getJobPositionId() != null) {
                        dto.setDepartmentName(idToDepartmentName.get(dto.getJobPositionId()));
                    }
                    return dto;
                })
                .toList());

        return paginationDTO;
    }

    public ApplicationResponseDTO getApplicationById(Long id) throws IdInvalidException {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Đơn ứng tuyển không tồn tại"));
        return ApplicationResponseDTO.fromEntity(application);
    }

    @Transactional
    public ApplicationResponseDTO createApplication(CreateApplicationDTO dto) throws IOException, IdInvalidException {
        String cvUrl = cloudinaryService.uploadFile(dto.getCvFile());

        // Link hoặc tạo Candidate theo email
        boolean existsCandidate = candidateService.existsByEmail(dto.getEmail());

        Application application = new Application();
        application.setJobPositionId(dto.getJobPositionId());
        application.setAppliedDate(LocalDate.now());
        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setCreatedBy(dto.getCreatedBy());

        application.setResumeUrl(cvUrl);
        application.setNotes(dto.getNotes());
        Candidate candidate = new Candidate();
        if (existsCandidate) {
            candidate = candidateService.findByEmail(dto.getEmail());
        } else {
            candidate.setStage(CandidateStage.NEW);
            candidate.setEmail(dto.getEmail());
            candidate.setFullName(dto.getFullName());
            candidate.setPhone(dto.getPhone());
            candidate = candidateService.saveCandidate(candidate);
        }
        application.setCandidate(candidate);

        Application saved = applicationRepository.save(application);
        if (saved.getStatus() == ApplicationStatus.SUBMITTED) {
            applicationEventsProducer.publishApplicationSubmitted(saved.getJobPositionId());

            // Thông báo cho HR/Recruiter trong phòng ban của job position
            // String token = SecurityUtil.getCurrentUserJWT().orElse(null);
            // try {
            // var jobPositionResponse =
            // jobService.getJobPositionById(saved.getJobPositionId(), token);
            // if (jobPositionResponse.getStatusCode().is2xxSuccessful() &&
            // jobPositionResponse.getBody() != null) {
            // var jobPosition = jobPositionResponse.getBody();
            // if (jobPosition.has("departmentId")) {
            // Long departmentId = jobPosition.get("departmentId").asLong();
            // String jobTitle = jobPosition.has("title") ?
            // jobPosition.get("title").asText() : "Vị trí #" + saved.getJobPositionId();
            // String candidateName = saved.getCandidate() != null ?
            // saved.getCandidate().getFullName() : "Ứng viên";

            // // Gửi thông báo cho phòng ban (HR/Recruiter)
            // notificationProducer.sendNotificationToDepartment(
            // departmentId,
            // null, // Có thể filter theo positionId nếu cần
            // "Có đơn ứng tuyển mới",
            // "Ứng viên '" + candidateName + "' đã nộp đơn ứng tuyển cho vị trí '" +
            // jobTitle + "'.",
            // token
            // );
            // }
            // }
            // } catch (Exception e) {
            // // Log error nhưng không fail toàn bộ process
            // System.err.println("Error sending notification for new application: " +
            // e.getMessage());
            // }
        }
        return ApplicationResponseDTO.fromEntity(saved);
    }

    @Transactional
    public ApplicationResponseDTO updateApplication(Long id, UpdateApplicationDTO dto)
            throws IdInvalidException, IOException {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Đơn ứng tuyển không tồn tại"));

        if (dto.getStatus() != null)
            application.setStatus(dto.getStatus());
        if (dto.getFeedback() != null)
            application.setFeedback(dto.getFeedback());
        if (dto.getRejectionReason() != null)
            application.setRejectionReason(dto.getRejectionReason());
        if (dto.getNotes() != null)
            application.setNotes(dto.getNotes());
        if (dto.getPriority() != null)
            application.setPriority(dto.getPriority());
        Candidate candidate = application.getCandidate();
        if (candidate != null) {
            if (!candidate.getEmail().equals(dto.getEmail())) {
                candidate.setEmail(dto.getEmail());
            }
            if (!candidate.getPhone().equals(dto.getPhone())) {
                candidate.setPhone(dto.getPhone());
            }
            if (!candidate.getFullName().equals(dto.getFullName())) {
                candidate.setFullName(dto.getFullName());
            }
        } else {
            candidate = new Candidate();
            candidate.setEmail(dto.getEmail());
            candidate.setFullName(dto.getFullName());
            candidate.setPhone(dto.getPhone());
        }
        candidate = candidateService.saveCandidate(candidate);

        application.setCandidate(candidate);
        if (dto.getCvFile() != null && !dto.getCvFile().isEmpty()) {
            String cvUrl = cloudinaryService.uploadFile(dto.getCvFile());
            application.setResumeUrl(cvUrl);
        }
        application.setUpdatedBy(dto.getUpdatedBy());
        Application saved = applicationRepository.save(application);
        return ApplicationResponseDTO.fromEntity(saved);
    }

    @Transactional
    public ApplicationResponseDTO updateApplicationStatus(Long id, String status, String feedback, Long updatedBy)
            throws IdInvalidException {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Đơn ứng tuyển không tồn tại"));

        application.setStatus(ApplicationStatus.valueOf(status));
        application.setFeedback(feedback);
        application.setUpdatedBy(updatedBy);
        Application savedApplication = applicationRepository.save(application);
        return ApplicationResponseDTO.fromEntity(savedApplication);
    }

    @Transactional
    public void deleteApplication(Long id) throws IdInvalidException {
        if (!applicationRepository.existsById(id)) {
            throw new IdInvalidException("Đơn ứng tuyển không tồn tại");
        }
        applicationRepository.deleteById(id);
    }

    @Transactional
    public ApplicationResponseDTO uploadCVWithFile(UploadCVDTO dto) throws IdInvalidException, IOException {
        String cvUrl = cloudinaryService.uploadFile(dto.getCvFile());

        // Link hoặc tạo Candidate theo email
        boolean existsCandidate = candidateService.existsByEmail(dto.getEmail());

        Application application = new Application();
        application.setJobPositionId(dto.getJobPositionId());
        application.setAppliedDate(LocalDate.now());
        application.setStatus(ApplicationStatus.SUBMITTED);

        application.setResumeUrl(cvUrl);
        application.setNotes(dto.getNotes());
        Candidate candidate = new Candidate();

        if (existsCandidate) {
            candidate = candidateService.findByEmail(dto.getEmail());
        } else {
            candidate.setStage(CandidateStage.NEW);
            candidate.setEmail(dto.getEmail());
            candidate.setFullName(dto.getFullName());
            candidate.setPhone(dto.getPhone());
            candidate = candidateService.saveCandidate(candidate);
        }

        application.setCandidate(candidate);

        Application saved = applicationRepository.save(application);
        if (saved.getStatus() == ApplicationStatus.SUBMITTED) {
            applicationEventsProducer.publishApplicationSubmitted(saved.getJobPositionId());

            // Thông báo cho HR/Recruiter trong phòng ban của job position
            // String token = SecurityUtil.getCurrentUserJWT().orElse(null);
            // try {
            // var jobPositionResponse =
            // jobService.getJobPositionById(saved.getJobPositionId(), token);
            // if (jobPositionResponse.getStatusCode().is2xxSuccessful() &&
            // jobPositionResponse.getBody() != null) {
            // var jobPosition = jobPositionResponse.getBody();
            // if (jobPosition.has("departmentId")) {
            // Long departmentId = jobPosition.get("departmentId").asLong();
            // String jobTitle = jobPosition.has("title") ?
            // jobPosition.get("title").asText() : "Vị trí #" + saved.getJobPositionId();
            // String candidateName = saved.getCandidate() != null ?
            // saved.getCandidate().getFullName() : "Ứng viên";

            // // Gửi thông báo cho phòng ban (HR/Recruiter)
            // notificationProducer.sendNotificationToDepartment(
            // departmentId,
            // null, // Có thể filter theo positionId nếu cần
            // "Có đơn ứng tuyển mới",
            // "Ứng viên '" + candidateName + "' đã nộp đơn ứng tuyển cho vị trí '" +
            // jobTitle + "'.",
            // token
            // );
            // }
            // }
            // } catch (Exception e) {
            // // Log error nhưng không fail toàn bộ process
            // System.err.println("Error sending notification for new application: " +
            // e.getMessage());
            // }
        }
        return ApplicationResponseDTO.fromEntity(saved);
    }

    @Transactional
    public ApplicationResponseDTO acceptApplication(Long applicationId, String feedback) throws IdInvalidException {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IdInvalidException("Đơn ứng tuyển không tồn tại"));

        // Cập nhật trạng thái application
        application.setStatus(ApplicationStatus.INTERVIEW);
        application.setFeedback(feedback);

        // Cập nhật trạng thái candidate thành "SHORTLISTED"
        Candidate candidate = application.getCandidate();
        candidate.setStage(CandidateStage.INTERVIEW1);
        candidateService.saveCandidate(candidate);

        Application savedApplication = applicationRepository.save(application);

        // Thông báo cho ứng viên (nếu có email và có thể tìm được employeeId từ email)
        // String token = SecurityUtil.getCurrentUserJWT().orElse(null);
        // if (candidate != null && candidate.getEmail() != null) {
        // try {
        // Long candidateEmployeeId =
        // userService.getUserIdByEmail(candidate.getEmail());
        // if (candidateEmployeeId != null) {
        // String jobTitle = "vị trí ứng tuyển";
        // try {
        // var jobPositionResponse =
        // jobService.getJobPositionById(savedApplication.getJobPositionId(), token);
        // if (jobPositionResponse.getStatusCode().is2xxSuccessful() &&
        // jobPositionResponse.getBody() != null) {
        // var jobPosition = jobPositionResponse.getBody();
        // if (jobPosition.has("title")) {
        // jobTitle = jobPosition.get("title").asText();
        // }
        // }
        // } catch (Exception e) {
        // // Ignore
        // }

        // notificationProducer.sendNotification(
        // candidateEmployeeId,
        // "Đơn ứng tuyển đã được chấp nhận",
        // "Đơn ứng tuyển của bạn cho vị trí '" + jobTitle + "' đã được chấp nhận. Bạn
        // sẽ được mời phỏng vấn.",
        // token
        // );
        // }
        // } catch (Exception e) {
        // // Log error nhưng không fail toàn bộ process
        // System.err.println("Error sending notification for accepted application: " +
        // e.getMessage());
        // }
        // }

        return ApplicationResponseDTO.fromEntity(savedApplication);
    }

    @Transactional
    public ApplicationResponseDTO rejectApplication(Long applicationId, String rejectionReason)
            throws IdInvalidException {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IdInvalidException("Đơn ứng tuyển không tồn tại"));

        // Cập nhật trạng thái application
        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(rejectionReason);

        Application savedApplication = applicationRepository.save(application);

        // Thông báo cho ứng viên (nếu có email và có thể tìm được employeeId từ email)
        // String token = SecurityUtil.getCurrentUserJWT().orElse(null);
        // Candidate candidate = savedApplication.getCandidate();
        // if (candidate != null && candidate.getEmail() != null) {
        // try {
        // Long candidateEmployeeId =
        // userService.getUserIdByEmail(candidate.getEmail());
        // if (candidateEmployeeId != null) {
        // String jobTitle = "vị trí ứng tuyển";
        // try {
        // var jobPositionResponse =
        // jobService.getJobPositionById(savedApplication.getJobPositionId(), token);
        // if (jobPositionResponse.getStatusCode().is2xxSuccessful() &&
        // jobPositionResponse.getBody() != null) {
        // var jobPosition = jobPositionResponse.getBody();
        // if (jobPosition.has("title")) {
        // jobTitle = jobPosition.get("title").asText();
        // }
        // }
        // } catch (Exception e) {
        // // Ignore
        // }

        // String reasonMsg = rejectionReason != null ? " Lý do: " + rejectionReason :
        // "";
        // notificationProducer.sendNotification(
        // candidateEmployeeId,
        // "Đơn ứng tuyển đã bị từ chối",
        // "Đơn ứng tuyển của bạn cho vị trí '" + jobTitle + "' đã bị từ chối." +
        // reasonMsg,
        // token
        // );
        // }
        // } catch (Exception e) {
        // // Log error nhưng không fail toàn bộ process
        // System.err.println("Error sending notification for rejected application: " +
        // e.getMessage());
        // }
        // }

        return ApplicationResponseDTO.fromEntity(savedApplication);
    }

    public ApplicationDetailResponseDTO getApplicationDetailById(Long id, String token) throws IdInvalidException {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Đơn ứng tuyển không tồn tại"));
        ApplicationDetailResponseDTO dto = new ApplicationDetailResponseDTO();
        dto.setId(application.getId());
        dto.setAppliedDate(application.getAppliedDate());
        dto.setStatus(application.getStatus());
        dto.setPriority(application.getPriority());
        dto.setRejectionReason(application.getRejectionReason());
        dto.setResumeUrl(application.getResumeUrl());
        dto.setFeedback(application.getFeedback());
        dto.setNotes(application.getNotes());
        if (application.getCandidate() != null) {
            dto.setFullName(application.getCandidate().getFullName());
            dto.setEmail(application.getCandidate().getEmail());
            dto.setPhone(application.getCandidate().getPhone());
        }
        // Comments
        if (application.getComments() != null) {
            dto.setComments(commentService.getByApplicationId(application.getId(), token));
        }
        // Reviews
        try {
            dto.setReviews(reviewService.getByApplicationId(application.getId(), token));
        } catch (Exception e) {
            // Nếu có lỗi, set empty list
            dto.setReviews(new ArrayList<>());
        }
        // JobPosition from job service (call by positionId)
        if (application.getJobPositionId() != null) {
            dto.setJobPosition(jobService.getJobPositionById(application.getJobPositionId(), token).getBody());
        }
        // Upcoming schedules for this candidate (from communications-service)
        if (application.getCandidate() != null && application.getCandidate().getId() != null) {
            var res = communicationService.getUpcomingSchedulesForCandidate(application.getCandidate().getId(), token);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                if (res.getBody().isArray()) {
                    List<Object> list = new ArrayList<>();
                    res.getBody().forEach(list::add);
                    dto.setUpcomingSchedules(list);
                } else if (res.getBody().has("data")) {
                    // trường hợp dịch vụ trả về có field data
                    List<Object> list = new ArrayList<>();
                    res.getBody().get("data").forEach(list::add);
                    dto.setUpcomingSchedules(list);
                }
            }
        }
        return dto;
    }
}
