package com.example.job_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.job_service.utils.enums.OfferStatus;

@Entity
@Table(name = "offers")
@Getter
@Setter
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID của ứng viên
     */
    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    /**
     * ID của vị trí (position)
     */
    @Column(name = "position_id", nullable = false)
    private Long positionId;

    /**
     * Ngày bắt đầu thử việc
     */
    @Column(name = "probation_start_date")
    private LocalDate probationStartDate;

    /**
     * Ghi chú về lương
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;

    /**
     * Người tạo yêu cầu
     */
    @Column(name = "requester_id")
    private Long requesterId;

    /**
     * Người chịu trách nhiệm chính (Submitter/Owner)
     * Thường là giống requesterId, nhưng có thể khác nếu thư ký nhập hộ sếp
     */
    @Column(name = "owner_user_id")
    private Long ownerUserId;

    /**
     * ID của workflow
     */
    @Column(name = "workflow_id")
    private Long workflowId;

    /**
     * ID của department
     */
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * Bước workflow hiện tại
     * NULL nếu chưa submit hoặc đã kết thúc
     */
    @Column(name = "current_step_id")
    private Long currentStepId;

    /**
     * Ngày gửi đi (khác ngày tạo nháp)
     * NULL nếu chưa submit (status = DRAFT)
     */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /**
     * Trạng thái active
     */
    private Boolean isActive = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
