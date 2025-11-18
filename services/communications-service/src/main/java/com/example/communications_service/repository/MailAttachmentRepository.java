package com.example.communications_service.repository;

import com.example.communications_service.model.MailAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailAttachmentRepository extends JpaRepository<MailAttachment, Long> {
    List<MailAttachment> findByMailMessageId(Long mailMessageId);
}
