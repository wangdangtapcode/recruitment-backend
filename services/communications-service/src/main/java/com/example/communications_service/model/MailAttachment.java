package com.example.communications_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mail_attachment")
@Getter
@Setter
public class MailAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_message_id", nullable = false)
    private MailMessage mailMessage;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl; // URL từ Cloudinary

    @Column(name = "file_path", columnDefinition = "TEXT")
    private String filePath; // Đường dẫn lưu file trên server (deprecated, giữ lại để tương thích)

    @Column(name = "cloudinary_public_id", columnDefinition = "TEXT")
    private String cloudinaryPublicId; // Public ID từ Cloudinary để có thể xóa sau này

    @Column(name = "file_size")
    private Long fileSize; // Kích thước file tính bằng bytes

    @Column(name = "content_type")
    private String contentType; // MIME type: image/png, application/pdf, etc.

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
