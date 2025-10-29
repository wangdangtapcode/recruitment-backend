package com.example.communications_service.service;

import com.example.communications_service.dto.Meta;
import com.example.communications_service.dto.PaginationDTO;
import com.example.communications_service.model.MailMessage;
import com.example.communications_service.repository.MailMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class MailboxService {

    private final MailMessageRepository mailRepo;
    private final EmailService emailService;

    public MailboxService(MailMessageRepository mailRepo, EmailService emailService) {
        this.mailRepo = mailRepo;
        this.emailService = emailService;
    }

    public MailMessage sendInternal(Long fromUserId, Long toUserId, String subject, String content, String threadId) {
        MailMessage msg = new MailMessage();
        msg.setFromUserId(fromUserId);
        msg.setToUserId(toUserId);
        msg.setToType("USER");
        msg.setSubject(subject);
        msg.setContent(content);
        msg.setThreadId(threadId != null ? threadId : UUID.randomUUID().toString());
        return mailRepo.save(msg);
    }

    public void sendToCandidate(String toEmail, String subject, String content) {
        emailService.sendSimpleEmail(toEmail, subject, content);
    }

    public PaginationDTO getInbox(Long userId, int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.min(Math.max(1, limit), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MailMessage> p = mailRepo.findByToUserIdAndToTypeOrderByCreatedAtDesc(userId, "USER", pageable);
        Meta meta = new Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(p.getTotalElements());
        meta.setPages(p.getTotalPages());
        PaginationDTO dto = new PaginationDTO();
        dto.setMeta(meta);
        dto.setResult(p.getContent());
        return dto;
    }

    public PaginationDTO getSent(Long userId, int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.min(Math.max(1, limit), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MailMessage> p = mailRepo.findByFromUserIdOrderByCreatedAtDesc(userId, pageable);
        Meta meta = new Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(p.getTotalElements());
        meta.setPages(p.getTotalPages());
        PaginationDTO dto = new PaginationDTO();
        dto.setMeta(meta);
        dto.setResult(p.getContent());
        return dto;
    }

    public PaginationDTO getThread(String threadId, int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.min(Math.max(1, limit), 100),
                Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<MailMessage> p = mailRepo.findByThreadIdOrderByCreatedAtAsc(threadId, pageable);
        Meta meta = new Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(p.getTotalElements());
        meta.setPages(p.getTotalPages());
        PaginationDTO dto = new PaginationDTO();
        dto.setMeta(meta);
        dto.setResult(p.getContent());
        return dto;
    }

    public void markRead(Long id) {
        mailRepo.findById(id).ifPresent(m -> {
            m.setRead(true);
            mailRepo.save(m);
        });
    }
}
