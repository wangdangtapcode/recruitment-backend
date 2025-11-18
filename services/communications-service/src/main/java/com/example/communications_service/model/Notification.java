package com.example.communications_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "recipient_id")
    private Long recipientId; 

    // Có thể thêm loại người nhận để phân biệt
    @Column(name = "recipient_type")
    private String recipientType; // "USER" hoặc "CANDIDATE"

    @Column(name = "notification_type")
    private String notificationType; // "EMAIL", "SMS", "IN_APP", "PUSH"

    @Column(name = "channel")
    private String channel; // "EMAIL", "SMS", "ZALO", "IN_APP"

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "is_delivered")
    private boolean isDelivered = false;

    @Column(name = "delivery_status")
    private String deliveryStatus; // "PENDING", "SENT", "DELIVERED", "FAILED", "BOUNCED"

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "external_id")
    private String externalId; // ID từ nhà cung cấp (SendGrid, Twilio, etc.)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Liên kết với mẫu đã tạo ra thông báo này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private NotificationTemplate template;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.deliveryStatus == null) {
            this.deliveryStatus = "PENDING";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}