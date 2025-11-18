package com.example.communications_service.service;

import com.example.communications_service.dto.Meta;
import com.example.communications_service.dto.PaginationDTO;
import com.example.communications_service.dto.mail.ForwardMailRequest;
import com.example.communications_service.dto.mail.MailTemplateResponse;
import com.example.communications_service.dto.mail.SendMailRequest;
import com.example.communications_service.model.MailAttachment;
import com.example.communications_service.model.MailMessage;
import com.example.communications_service.repository.MailAttachmentRepository;
import com.example.communications_service.repository.MailMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MailboxService {

    private final MailMessageRepository mailRepo;
    private final MailAttachmentRepository attachmentRepo;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    public MailboxService(MailMessageRepository mailRepo,
            MailAttachmentRepository attachmentRepo,
            EmailService emailService) {
        this.mailRepo = mailRepo;
        this.attachmentRepo = attachmentRepo;
        this.emailService = emailService;
        this.objectMapper = new ObjectMapper();
    }

    // Gửi email mới
    public MailMessage sendMail(SendMailRequest request) {
        // Nếu gửi qua Gmail, sử dụng EmailService
        if (request.isSendViaGmail() && request.getToEmail() != null) {
            return emailService.sendSimpleEmailAndSave(
                    request.getToEmail(),
                    request.getSubject(),
                    request.getContent(),
                    request.getFromUserId());
        }

        // Gửi nội bộ (chỉ lưu vào database)
        MailMessage msg = new MailMessage();
        msg.setFromUserId(request.getFromUserId());
        msg.setToUserId(request.getToUserId());
        msg.setToType("USER");
        msg.setSubject(request.getSubject());
        msg.setContent(request.getContent());

        // Xử lý links - chuyển List thành JSON string
        if (request.getLinks() != null && !request.getLinks().isEmpty()) {
            try {
                msg.setLinks(objectMapper.writeValueAsString(request.getLinks()));
            } catch (JsonProcessingException e) {
                msg.setLinks("[]");
            }
        }

        // Xử lý reply
        if (request.getReplyToId() != null) {
            msg.setReplyToId(request.getReplyToId());
            // Lấy threadId từ email được reply
            mailRepo.findById(request.getReplyToId()).ifPresent(repliedMsg -> {
                msg.setThreadId(repliedMsg.getThreadId());
            });
        } else {
            msg.setThreadId(request.getThreadId() != null ? request.getThreadId() : UUID.randomUUID().toString());
        }

        return mailRepo.save(msg);
    }

    // Reply email
    public MailMessage reply(Long replyToId, SendMailRequest request) {
        MailMessage originalMsg = mailRepo.findById(replyToId)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        request.setReplyToId(replyToId);
        request.setThreadId(originalMsg.getThreadId());

        // Thêm prefix "Re: " vào subject nếu chưa có
        String subject = originalMsg.getSubject();
        if (!subject.startsWith("Re: ")) {
            subject = "Re: " + subject;
        }
        request.setSubject(subject);

        return sendMail(request);
    }

    // Forward email
    public List<MailMessage> forward(ForwardMailRequest request) {
        MailMessage originalMsg = mailRepo.findById(request.getForwardFromId())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        // Thêm prefix "Fwd: " vào subject
        final String subject;
        String originalSubject = originalMsg.getSubject();
        if (!originalSubject.startsWith("Fwd: ")) {
            subject = "Fwd: " + originalSubject;
        } else {
            subject = originalSubject;
        }

        // Tạo email forward cho từng người nhận
        return request.getToUserIds().stream().map(toUserId -> {
            MailMessage forwardedMsg = new MailMessage();
            forwardedMsg.setFromUserId(request.getFromUserId());
            forwardedMsg.setToUserId(toUserId);
            forwardedMsg.setToType("USER");
            forwardedMsg.setSubject(subject);

            // Nội dung forward = nội dung thêm + nội dung email gốc
            String content = request.getContent() != null ? request.getContent() + "\n\n--- Forwarded message ---\n"
                    : "--- Forwarded message ---\n";
            content += originalMsg.getContent();
            forwardedMsg.setContent(content);

            // Copy links từ email gốc
            forwardedMsg.setLinks(originalMsg.getLinks());

            forwardedMsg.setForwardedFromId(request.getForwardFromId());
            forwardedMsg.setThreadId(UUID.randomUUID().toString());

            MailMessage saved = mailRepo.save(forwardedMsg);

            // Copy attachments từ email gốc
            originalMsg.getAttachments().forEach(originalAttachment -> {
                MailAttachment newAttachment = new MailAttachment();
                newAttachment.setMailMessage(saved);
                newAttachment.setFileName(originalAttachment.getFileName());
                newAttachment.setFileUrl(originalAttachment.getFileUrl());
                newAttachment.setFilePath(originalAttachment.getFilePath());
                newAttachment.setCloudinaryPublicId(originalAttachment.getCloudinaryPublicId());
                newAttachment.setFileSize(originalAttachment.getFileSize());
                newAttachment.setContentType(originalAttachment.getContentType());
                attachmentRepo.save(newAttachment);
            });

            return saved;
        }).toList();
    }

    // Lưu attachment với Cloudinary URL
    public MailAttachment saveAttachment(Long mailMessageId, String fileName, String fileUrl, String cloudinaryPublicId,
            Long fileSize, String contentType) {
        MailMessage mailMessage = mailRepo.findById(mailMessageId)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        MailAttachment attachment = new MailAttachment();
        attachment.setMailMessage(mailMessage);
        attachment.setFileName(fileName);
        attachment.setFileUrl(fileUrl);
        attachment.setCloudinaryPublicId(cloudinaryPublicId);
        attachment.setFileSize(fileSize);
        attachment.setContentType(contentType);

        return attachmentRepo.save(attachment);
    }

    // Lấy danh sách attachments của một email
    public List<MailAttachment> getAttachments(Long mailMessageId) {
        return attachmentRepo.findByMailMessageId(mailMessageId);
    }

    // Lấy attachment theo ID
    public MailAttachment getAttachmentById(Long attachmentId) {
        return attachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment không tồn tại"));
    }

    // Xóa email (soft delete)
    public void deleteMail(Long id, Long userId) {
        mailRepo.findById(id).ifPresent(m -> {
            // Chỉ cho phép xóa email của chính mình
            if (m.getFromUserId().equals(userId) || m.getToUserId().equals(userId)) {
                m.setDeleted(true);
                mailRepo.save(m);
            } else {
                throw new RuntimeException("Không có quyền xóa email này");
            }
        });
    }

    // Xóa vĩnh viễn
    public void permanentDelete(Long id, Long userId) {
        mailRepo.findById(id).ifPresent(m -> {
            if (m.getFromUserId().equals(userId) || m.getToUserId().equals(userId)) {
                mailRepo.delete(m);
            } else {
                throw new RuntimeException("Không có quyền xóa email này");
            }
        });
    }

    // Đánh dấu quan trọng
    public void markImportant(Long id, boolean important) {
        mailRepo.findById(id).ifPresent(m -> {
            m.setImportant(important);
            mailRepo.save(m);
        });
    }

    // Đánh dấu sao
    public void markStarred(Long id, boolean starred) {
        mailRepo.findById(id).ifPresent(m -> {
            m.setStarred(starred);
            mailRepo.save(m);
        });
    }

    // Lấy email theo ID
    public MailMessage getMailById(Long id) {
        return mailRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
    }

    // Lấy email template (JSON format) theo ID
    public MailTemplateResponse getMailTemplateById(Long id) {
        MailMessage mail = getMailById(id);
        return convertToTemplateResponse(mail);
    }

    // Chuyển đổi MailMessage thành MailTemplateResponse
    private MailTemplateResponse convertToTemplateResponse(MailMessage mail) {
        MailTemplateResponse response = new MailTemplateResponse();
        response.setId(mail.getId());
        response.setThreadId(mail.getThreadId());
        response.setFromUserId(mail.getFromUserId());
        response.setToUserId(mail.getToUserId());
        response.setSubject(mail.getSubject());
        response.setContent(mail.getContent());
        response.setRead(mail.isRead());
        response.setImportant(mail.isImportant());
        response.setStarred(mail.isStarred());
        response.setReplyToId(mail.getReplyToId());
        response.setForwardedFromId(mail.getForwardedFromId());
        response.setCreatedAt(mail.getCreatedAt());
        response.setUpdatedAt(mail.getUpdatedAt());

        // Parse links từ JSON string
        if (mail.getLinks() != null && !mail.getLinks().isEmpty()) {
            try {
                List<String> links = objectMapper.readValue(mail.getLinks(), new TypeReference<List<String>>() {
                });
                response.setLinks(links);
            } catch (JsonProcessingException e) {
                response.setLinks(List.of());
            }
        } else {
            response.setLinks(List.of());
        }

        // Convert attachments
        List<MailTemplateResponse.AttachmentInfo> attachmentInfos = mail.getAttachments().stream()
                .map(MailTemplateResponse.AttachmentInfo::from)
                .toList();
        response.setAttachments(attachmentInfos);

        return response;
    }

    // Lấy inbox (chỉ email chưa xóa)
    public PaginationDTO getInbox(Long employeeId, int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.min(Math.max(1, limit), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MailMessage> p = mailRepo.findByToUserIdAndToTypeAndDeletedFalseOrderByCreatedAtDesc(employeeId, "USER",
                pageable);
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

    // Lấy sent (chỉ email chưa xóa)
    public PaginationDTO getSent(Long employeeId, int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.min(Math.max(1, limit), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MailMessage> p = mailRepo.findByFromUserIdAndDeletedFalseOrderByCreatedAtDesc(employeeId, pageable);
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

    // Lấy email đã xóa
    public PaginationDTO getDeleted(Long employeeId, int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.min(Math.max(1, limit), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MailMessage> p = mailRepo.findByDeletedTrueAndFromUserIdOrDeletedTrueAndToUserIdOrderByCreatedAtDesc(
                employeeId, employeeId, pageable);
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

    // Lấy email quan trọng
    public PaginationDTO getImportant(Long employeeId, int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.min(Math.max(1, limit), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MailMessage> p = mailRepo
                .findByImportantTrueAndDeletedFalseAndFromUserIdOrImportantTrueAndDeletedFalseAndToUserIdOrderByCreatedAtDesc(
                        employeeId, employeeId, pageable);
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

    // Lấy email đã đánh dấu sao
    public PaginationDTO getStarred(Long employeeId, int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.min(Math.max(1, limit), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MailMessage> p = mailRepo
                .findByStarredTrueAndDeletedFalseAndFromUserIdOrStarredTrueAndDeletedFalseAndToUserIdOrderByCreatedAtDesc(
                        employeeId, employeeId, pageable);
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

    // Khôi phục email đã xóa
    public void restoreMail(Long id) {
        mailRepo.findById(id).ifPresent(m -> {
            m.setDeleted(false);
            mailRepo.save(m);
        });
    }

    // Đếm email chưa đọc
    public Long getUnreadCount(Long employeeId) {
        return mailRepo.countByToUserIdAndToTypeAndReadFalseAndDeletedFalse(employeeId, "USER");
    }

    /**
     * Lấy tất cả email với nhiều filter và phân trang
     * 
     * @param employeeId ID của nhân viên (bắt buộc)
     * @param folder     Folder: inbox, sent, deleted, important, starred, all (mặc
     *                   định: all)
     * @param read       true/false/null - lọc theo trạng thái đã đọc
     * @param important  true/false/null - lọc theo email quan trọng
     * @param starred    true/false/null - lọc theo email đã đánh dấu sao
     * @param external   true/false/null - lọc theo email bên ngoài
     * @param keyword    Từ khóa tìm kiếm trong subject (không search trong content
     *                   do CLOB)
     * @param sortBy     Trường để sort (mặc định: createdAt)
     * @param sortOrder  asc hoặc desc (mặc định: desc)
     * @param page       Số trang (mặc định: 1)
     * @param limit      Số lượng mỗi trang (mặc định: 10, tối đa: 100)
     * @return PaginationDTO chứa danh sách email và metadata
     */
    public PaginationDTO getAllEmailsWithFilters(
            Long employeeId,
            String folder,
            Boolean read,
            Boolean important,
            Boolean starred,
            Boolean external,
            String keyword,
            String sortBy,
            String sortOrder,
            int page,
            int limit) {

        // Validate và normalize parameters
        if (employeeId == null) {
            throw new RuntimeException("employeeId là bắt buộc");
        }

        if (folder == null || folder.isEmpty()) {
            folder = "all";
        }

        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "createdAt";
        }

        if (sortOrder == null || sortOrder.isEmpty()) {
            sortOrder = "desc";
        }

        // Validate pagination
        page = Math.max(1, page);
        limit = Math.min(Math.max(1, limit), 100);

        // Create sort
        Sort.Direction direction = sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);

        // Create pageable
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        // Execute query
        Page<MailMessage> mailPage = mailRepo.findAllWithFilters(
                employeeId,
                folder,
                read,
                important,
                starred,
                external,
                keyword != null && !keyword.isEmpty() ? keyword : null,
                pageable);

        // Build response
        Meta meta = new Meta();
        meta.setPage(page);
        meta.setPageSize(limit);
        meta.setTotal(mailPage.getTotalElements());
        meta.setPages(mailPage.getTotalPages());

        PaginationDTO dto = new PaginationDTO();
        dto.setMeta(meta);
        dto.setResult(mailPage.getContent());

        return dto;
    }

    // Phương thức cũ để tương thích
    public MailMessage sendInternal(Long fromEmployeeId, Long toEmployeeId, String subject, String content,
            String threadId) {
        SendMailRequest request = new SendMailRequest();
        request.setFromUserId(fromEmployeeId);
        request.setToUserId(toEmployeeId);
        request.setSubject(subject);
        request.setContent(content);
        request.setThreadId(threadId);
        return sendMail(request);
    }

    public void sendToCandidate(String toEmail, String subject, String content) {
        emailService.sendSimpleEmail(toEmail, subject, content);
    }
}
