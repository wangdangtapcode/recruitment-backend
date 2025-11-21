package com.example.communications_service.controller;

import com.example.communications_service.dto.PaginationDTO;
import com.example.communications_service.dto.mail.ForwardMailRequest;
import com.example.communications_service.dto.mail.SendMailRequest;
import com.example.communications_service.model.MailAttachment;
import com.example.communications_service.model.MailMessage;
import com.example.communications_service.service.MailboxService;
import com.example.communications_service.utils.SecurityUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.communications_service.service.CloudinaryService;
import com.example.communications_service.dto.mail.MailTemplateResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/communications-service/mail")
public class MailController {

    private final MailboxService mailboxService;
    private final CloudinaryService cloudinaryService;

    public MailController(MailboxService mailboxService, CloudinaryService cloudinaryService) {
        this.mailboxService = mailboxService;
        this.cloudinaryService = cloudinaryService;
    }

    // ==================== GỬI EMAIL (SEND EMAIL) ====================

    // Gửi email mới (nội bộ hoặc qua Gmail)
    @PostMapping("/send")
    public ResponseEntity<MailMessage> sendMail(@RequestBody SendMailRequest request) {
        Long employeeId = SecurityUtil.extractEmployeeId();
        if (employeeId == null) {
            throw new RuntimeException("Không tìm thấy employeeId trong token");
        }
        request.setFromUserId(employeeId);
        return ResponseEntity.ok(mailboxService.sendMail(request));
    }

    // Gửi email thực qua Gmail
    @PostMapping("/send/gmail")
    public ResponseEntity<MailMessage> sendEmailViaGmail(@RequestBody SendMailRequest request) {
        Long employeeId = SecurityUtil.extractEmployeeId();
        if (employeeId == null) {
            throw new RuntimeException("Không tìm thấy employeeId trong token");
        }
        request.setFromUserId(employeeId);
        request.setSendViaGmail(true);
        return ResponseEntity.ok(mailboxService.sendMail(request));
    }

    // Reply email
    @PostMapping("/reply/{replyToId}")
    public ResponseEntity<MailMessage> reply(
            @PathVariable Long replyToId,
            @RequestBody SendMailRequest request) {
        return ResponseEntity.ok(mailboxService.reply(replyToId, request));
    }

    // Forward email
    @PostMapping("/forward")
    public ResponseEntity<List<MailMessage>> forward(@RequestBody ForwardMailRequest request) {
        return ResponseEntity.ok(mailboxService.forward(request));
    }

    // Endpoint cũ để tương thích - Gửi email nội bộ
    @PostMapping("/send/internal")
    public ResponseEntity<MailMessage> sendInternal(@RequestBody Map<String, Object> req) {
        Long fromEmployeeId = Long.valueOf(req.get("fromEmployeeId").toString());
        Long toEmployeeId = Long.valueOf(req.get("toEmployeeId").toString());
        String subject = String.valueOf(req.getOrDefault("subject", ""));
        String content = String.valueOf(req.getOrDefault("content", ""));
        String threadId = (String) req.getOrDefault("threadId", null);
        return ResponseEntity.ok(mailboxService.sendInternal(fromEmployeeId, toEmployeeId, subject, content, threadId));
    }

    // ==================== QUẢN LÝ FILE ĐÍNH KÈM (ATTACHMENTS) ====================

    // Upload file đính kèm lên Cloudinary
    @PostMapping("/{mailMessageId}/attachments")
    public ResponseEntity<MailAttachment> uploadAttachment(
            @PathVariable Long mailMessageId,
            @RequestParam("file") MultipartFile file) {
        try {
            // Upload file lên Cloudinary và lấy cả URL và public_id
            Map<String, Object> uploadResult = cloudinaryService.upload(file);
            String fileUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            // Lưu thông tin attachment vào database
            MailAttachment attachment = mailboxService.saveAttachment(
                    mailMessageId,
                    file.getOriginalFilename(),
                    fileUrl,
                    publicId,
                    file.getSize(),
                    file.getContentType());

            return ResponseEntity.ok(attachment);
        } catch (Exception e) {
            throw new RuntimeException("Không thể upload file lên Cloudinary: " + e.getMessage());
        }
    }

    // Lấy URL download file đính kèm từ Cloudinary
    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Map<String, String>> getAttachmentUrl(@PathVariable Long attachmentId) {
        MailAttachment attachment = mailboxService.getAttachmentById(attachmentId);
        return ResponseEntity.ok(Map.of(
                "url", attachment.getFileUrl(),
                "fileName", attachment.getFileName(),
                "contentType", attachment.getContentType() != null ? attachment.getContentType() : ""));
    }

    // Lấy danh sách attachments của một email
    @GetMapping("/{mailMessageId}/attachments")
    public ResponseEntity<List<MailAttachment>> getAttachments(@PathVariable Long mailMessageId) {
        return ResponseEntity.ok(mailboxService.getAttachments(mailMessageId));
    }

    // // Lấy email theo ID
    // @GetMapping("/{id}")
    // public ResponseEntity<MailMessage> getMailById(@PathVariable Long id) {
    // return ResponseEntity.ok(mailboxService.getMailById(id));
    // }

    // ==================== NHẬN EMAIL (RECEIVE EMAIL) ====================

    // Lấy email template (JSON format) theo ID
    @GetMapping("/{id}/template")
    public ResponseEntity<MailTemplateResponse> getMailTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(mailboxService.getMailTemplateById(id));
    }

    // Inbox - Hộp thư đến (email đã nhận)
    @GetMapping("/inbox")
    public ResponseEntity<PaginationDTO> inbox(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Long employeeId = SecurityUtil.extractEmployeeId();
        if (employeeId == null) {
            throw new RuntimeException("Không tìm thấy employeeId trong token");
        }
        return ResponseEntity.ok(mailboxService.getInbox(employeeId, page, limit));
    }

    // Sent - Hộp thư đã gửi (email đã gửi)
    @GetMapping("/sent")
    public ResponseEntity<PaginationDTO> sent(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Long employeeId = SecurityUtil.extractEmployeeId();
        if (employeeId == null) {
            throw new RuntimeException("Không tìm thấy employeeId trong token");
        }
        return ResponseEntity.ok(mailboxService.getSent(employeeId, page, limit));
    }

    // Deleted - Hộp thư đã xóa
    @GetMapping("/deleted/{employeeId}")
    public ResponseEntity<PaginationDTO> deleted(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(mailboxService.getDeleted(employeeId, page, limit));
    }

    // Important - Email quan trọng
    @GetMapping("/important/{employeeId}")
    public ResponseEntity<PaginationDTO> important(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(mailboxService.getImportant(employeeId, page, limit));
    }

    // Starred - Email đã đánh dấu sao
    @GetMapping("/starred/{employeeId}")
    public ResponseEntity<PaginationDTO> starred(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(mailboxService.getStarred(employeeId, page, limit));
    }

    // Thread - Chuỗi email (conversation)
    @GetMapping("/threads/{threadId}")
    public ResponseEntity<PaginationDTO> thread(
            @PathVariable String threadId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(mailboxService.getThread(threadId, page, limit));
    }

    // Đếm email chưa đọc
    @GetMapping("/unread-count/{employeeId}")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long employeeId) {
        Long count = mailboxService.getUnreadCount(employeeId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // Get all emails với nhiều filter và phân trang (hỗ trợ cả inbox và sent)
    @GetMapping()
    public ResponseEntity<PaginationDTO> getAllEmails(
            @RequestParam(required = false) String folder, // inbox, sent, deleted, important, starred, all
            @RequestParam(required = false) Boolean read, // true, false, null
            @RequestParam(required = false) Boolean important, // true, false, null
            @RequestParam(required = false) Boolean starred, // true, false, null
            @RequestParam(required = false) Boolean external, // true, false, null
            @RequestParam(required = false) String keyword, // Tìm kiếm trong subject
            @RequestParam(defaultValue = "createdAt") String sortBy, // Trường để sort
            @RequestParam(defaultValue = "desc") String sortOrder, // asc hoặc desc
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Long employeeId = SecurityUtil.extractEmployeeId();
        if (employeeId == null) {
            throw new RuntimeException("Không tìm thấy employeeId trong token");
        }
        return ResponseEntity.ok(mailboxService.getAllEmailsWithFilters(
                employeeId, folder, read, important, starred, external, keyword,
                sortBy, sortOrder, page, limit));
    }

    // ==================== QUẢN LÝ TRẠNG THÁI EMAIL ====================

    // Đánh dấu đã đọc
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        mailboxService.markRead(id);
        return ResponseEntity.ok().build();
    }

    // Đánh dấu quan trọng
    @PutMapping("/{id}/important")
    public ResponseEntity<Void> markImportant(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean important) {
        mailboxService.markImportant(id, important);
        return ResponseEntity.ok().build();
    }

    // Đánh dấu sao
    @PutMapping("/{id}/starred")
    public ResponseEntity<Void> markStarred(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean starred) {
        mailboxService.markStarred(id, starred);
        return ResponseEntity.ok().build();
    }

    // Xóa email (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMail(
            @PathVariable Long id,
            @RequestParam Long userId) {
        mailboxService.deleteMail(id, userId);
        return ResponseEntity.ok().build();
    }

    // Xóa vĩnh viễn
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentDelete(
            @PathVariable Long id,
            @RequestParam Long userId) {
        mailboxService.permanentDelete(id, userId);
        return ResponseEntity.ok().build();
    }

    // Khôi phục email đã xóa
    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restoreMail(@PathVariable Long id) {
        mailboxService.restoreMail(id);
        return ResponseEntity.ok().build();
    }
}
