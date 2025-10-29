package com.example.communications_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @Column(name = "to_user_id")
    private Long toUserId;

    @Column(name = "to_type")
    private String toType; // USER or CANDIDATE

    @Column(name = "subject")
    private String subject;

    @Lob
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read")
    private boolean read;

    @Column(name = "is_deleted")
    private boolean deleted;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.read = false;
        this.deleted = false;
    }
}
