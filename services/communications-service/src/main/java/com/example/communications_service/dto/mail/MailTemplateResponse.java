package com.example.communications_service.dto.mail;

import com.example.communications_service.model.MailAttachment;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class MailTemplateResponse {
    private Long id;
    private String threadId;
    private Long fromUserId;
    private Long toUserId;
    private String subject;
    private String content;
    private List<String> links;
    private List<AttachmentInfo> attachments;
    private boolean read;
    private boolean important;
    private boolean starred;
    private Long replyToId;
    private Long forwardedFromId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    public static class AttachmentInfo {
        private Long id;
        private String fileName;
        private String fileUrl;
        private Long fileSize;
        private String contentType;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        public static AttachmentInfo from(MailAttachment attachment) {
            AttachmentInfo info = new AttachmentInfo();
            info.setId(attachment.getId());
            info.setFileName(attachment.getFileName());
            info.setFileUrl(attachment.getFileUrl());
            info.setFileSize(attachment.getFileSize());
            info.setContentType(attachment.getContentType());
            info.setCreatedAt(attachment.getCreatedAt());
            return info;
        }
    }
}

