package com.example.workflow_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.workflow_service.utils.enums.ApprovalStatus;

import java.time.OffsetDateTime;

@Entity
@Table(name = "approval_trackings")
@Getter
@Setter
public class ApprovalTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID của yêu cầu (vd: recruitment_request.id = 123)
    @Column(name = "request_id", nullable = false)
    private Long requestId;

    // ID của bước workflow hiện tại
    @Column(name = "step_id", nullable = false)
    private Long stepId;

    /**
     * Bước workflow hiện tại của request (để track tuần tự)
     * Có thể khác step_id nếu đang ở bước khác
     */
    @Column(name = "current_step_id")
    private Long currentStepId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    /**
     * Loại hành động: APPROVE, REJECT, RETURN, CANCEL
     */
    @Column(name = "action_type", length = 20)
    private String actionType;

    /**
     * Bước được trả về (khi actionType = 'RETURN')
     */
    @Column(name = "returned_to_step_id")
    private Long returnedToStepId;

    /**
     * Thời điểm bị trả về
     */
    @Column(name = "returned_at")
    private OffsetDateTime returnedAt;

    /**
     * Bước hủy request (khi actionType = 'CANCEL')
     */
    @Column(name = "cancelled_by_step_id")
    private Long cancelledByStepId;

    /**
     * User hủy request
     */
    @Column(name = "cancelled_by_user_id")
    private Long cancelledByUserId;

    /**
     * Thời điểm hủy
     */
    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    // User được gán để phê duyệt (tìm từ User_Positions dựa trên
    // approver_position_id)
    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    // User thực hiện hành động (approve/reject)
    @Column(name = "action_user_id")
    private Long actionUserId;

    // Thời gian thực hiện hành động
    @Column(name = "action_at")
    private OffsetDateTime actionAt;

    // Ghi chú phê duyệt/từ chối
    @Column(columnDefinition = "MEDIUMTEXT")
    private String notes;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
