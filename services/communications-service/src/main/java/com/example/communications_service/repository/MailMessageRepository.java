package com.example.communications_service.repository;

import com.example.communications_service.model.MailMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailMessageRepository extends JpaRepository<MailMessage, Long> {

    Page<MailMessage> findByToUserIdAndToTypeOrderByCreatedAtDesc(Long toUserId, String toType, Pageable pageable);

    Page<MailMessage> findByFromUserIdOrderByCreatedAtDesc(Long fromUserId, Pageable pageable);

    Page<MailMessage> findByThreadIdOrderByCreatedAtAsc(String threadId, Pageable pageable);
}
