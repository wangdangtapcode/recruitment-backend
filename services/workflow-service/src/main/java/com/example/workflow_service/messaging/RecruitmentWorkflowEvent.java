package com.example.workflow_service.messaging;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecruitmentWorkflowEvent {

    private String eventType;
    private Long requestId;
    private Long workflowId;
    private Long currentStepId; // Bước workflow hiện tại đang xử lý
    private Long actorUserId;
    private String notes;
    private String reason;
    private String requestStatus;
    private Long ownerUserId;
    private Long requesterId;
    private Long departmentId;
    private LocalDateTime occurredAt;
    private String authToken;

    // Cho trường hợp RETURN
    private Long returnedToStepId; // Bước cần trả về (null = trả về bước đầu tiên)

    // Cho trường hợp APPROVE/REJECT ở bước cụ thể
    private Long stepId; // ID của bước đang approve/reject
    private Boolean approved; // true = approve, false = reject
}
