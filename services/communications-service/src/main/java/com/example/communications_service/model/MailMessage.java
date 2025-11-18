package com.example.communications_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mail_message")
@Getter
@Setter
public class MailMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "thread_id")
    private String threadId;

    @Column(name = "from_user_id")
    private Long fromUserId;

    @Column(name = "from_email")
    private String fromEmail; // Email thực của người gửi

    @Column(name = "to_user_id")
    private Long toUserId;

    @Column(name = "to_email")
    private String toEmail; // Email thực của người nhận

    @Column(name = "to_type")
    private String toType; // "USER" hoặc "CANDIDATE"

    @Column(name = "is_external")
    private boolean external; // true nếu gửi/nhận từ email bên ngoài (không phải nhân viên)

    @Column(name = "subject")
    private String subject;

    @Lob
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Lob
    @Column(name = "links", columnDefinition = "TEXT")
    private String links; // JSON array of links: ["url1", "url2"]

    @Column(name = "is_read")
    private boolean read;

    @Column(name = "is_deleted")
    private boolean deleted;

    @Column(name = "is_important")
    private boolean important;

    @Column(name = "is_starred")
    private boolean starred;

    @Column(name = "reply_to_id")
    private Long replyToId; // ID của email được reply

    @Column(name = "forwarded_from_id")
    private Long forwardedFromId; // ID của email được forward

    @Column(name = "gmail_message_id")
    private String gmailMessageId; // Message ID từ Gmail để tránh duplicate

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "mailMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MailAttachment> attachments = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.read = false;
        this.deleted = false;
        this.important = false;
        this.starred = false;
        this.external = false;
        if (this.toType == null) {
            this.toType = "USER";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
