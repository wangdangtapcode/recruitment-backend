package com.example.job_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.job_service.model.RecruitmentRequest;
import com.example.job_service.repository.RecruitmentRequestRepository;
import com.example.job_service.utils.enums.RecruitmentRequestStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Component
public class WorkflowCompletionListener {

    private static final Logger log = LoggerFactory.getLogger(WorkflowCompletionListener.class);

    private final ObjectMapper objectMapper;
    private final RecruitmentRequestRepository recruitmentRequestRepository;

    public WorkflowCompletionListener(ObjectMapper objectMapper,
            RecruitmentRequestRepository recruitmentRequestRepository) {
        this.objectMapper = objectMapper;
        this.recruitmentRequestRepository = recruitmentRequestRepository;
    }

    @KafkaListener(topics = "${kafka.topic.recruitment-workflow:recruitment-workflow-events}", groupId = "${spring.kafka.consumer.group-id:job-service}")
    @Transactional
    public void handleWorkflowEvent(String message) {
        try {
            RecruitmentWorkflowEvent event = objectMapper.readValue(message, RecruitmentWorkflowEvent.class);

            // Chỉ xử lý event WORKFLOW_COMPLETED
            if ("WORKFLOW_COMPLETED".equals(event.getEventType())) {
                log.info("Received WORKFLOW_COMPLETED event for request {}", event.getRequestId());

                RecruitmentRequest request = recruitmentRequestRepository.findById(event.getRequestId())
                        .orElse(null);

                if (request != null && (request.getStatus() == RecruitmentRequestStatus.SUBMITTED
                        || request.getStatus() == RecruitmentRequestStatus.PENDING)) {
                    request.setStatus(RecruitmentRequestStatus.APPROVED);
                    request.setApprovedId(event.getActorUserId());
                    request.setApprovedAt(LocalDateTime.now());
                    request.setApprovalNotes(event.getNotes());
                    request.setCurrentStepId(null); // Không còn bước nào
                    recruitmentRequestRepository.save(request);
                    log.info("Request {} đã được chuyển sang APPROVED sau khi hoàn thành workflow",
                            event.getRequestId());
                }
            }
        } catch (Exception e) {
            log.error("Error processing workflow completion event: {}", message, e);
        }
    }
}
