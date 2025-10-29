package com.example.communications_service.controller;

import com.example.communications_service.dto.BulkEmailRequest;
import com.example.communications_service.model.Notification;
import com.example.communications_service.service.EmailService;
import com.example.communications_service.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/communications-service/emails")
@CrossOrigin(origins = "*")
public class EmailController {

    private final EmailService emailService;
    private final NotificationService notificationService;

    public EmailController(EmailService emailService, NotificationService notificationService) {
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String subject = request.get("subject");
        String message = request.get("message");

        emailService.sendSimpleEmail(to, subject, message);
        return ResponseEntity.ok("Email sent successfully");
    }

    @PostMapping("/send-html")
    public ResponseEntity<String> sendHtmlEmail(@RequestBody Map<String, String> request) {
        try {
            String to = request.get("to");
            String subject = request.get("subject");
            String htmlContent = request.get("htmlContent");

            emailService.sendHtmlEmail(to, subject, htmlContent);
            return ResponseEntity.ok("HTML email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send HTML email: " + e.getMessage());
        }
    }

    @PostMapping("/send-template")
    public ResponseEntity<String> sendTemplateEmail(@RequestBody Map<String, Object> request) {
        try {
            String to = (String) request.get("to");
            String subject = (String) request.get("subject");
            String templateName = (String) request.get("templateName");
            @SuppressWarnings("unchecked")
            Map<String, Object> variables = (Map<String, Object>) request.get("variables");

            emailService.sendTemplateEmail(to, subject, templateName, variables);
            return ResponseEntity.ok("Template email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send template email: " + e.getMessage());
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<String> sendBulkEmails(@Valid @RequestBody BulkEmailRequest request) {
        try {
            String[] recipients = request.getRecipients().toArray(new String[0]);

            if (request.getTemplateName() != null && !request.getTemplateName().isEmpty()) {
                emailService.sendBulkEmails(recipients, request.getSubject(),
                        request.getTemplateName(), request.getVariables());
            } else {
                // Send simple emails
                for (String recipient : recipients) {
                    emailService.sendSimpleEmail(recipient, request.getSubject(), request.getMessage());
                }
            }

            return ResponseEntity.ok("Bulk emails sent successfully to " + recipients.length + " recipients");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send bulk emails: " + e.getMessage());
        }
    }

    @PostMapping("/candidate-welcome")
    public ResponseEntity<String> sendCandidateWelcomeEmail(@RequestBody Map<String, String> request) {
        try {
            String candidateEmail = request.get("candidateEmail");
            String candidateName = request.get("candidateName");

            String subject = "Chào mừng bạn đến với hệ thống tuyển dụng";
            String message = "Xin chào " + candidateName + ",\n\n" +
                    "Cảm ơn bạn đã đăng ký với chúng tôi. Chúng tôi sẽ liên hệ với bạn sớm nhất có thể.\n\n" +
                    "Trân trọng,\n" +
                    "Đội ngũ tuyển dụng";

            emailService.sendSimpleEmail(candidateEmail, subject, message);
            return ResponseEntity.ok("Welcome email sent to candidate");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send welcome email: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<com.example.communications_service.dto.PaginationDTO> getSentEmails(
            @RequestParam(name = "recipientId", required = false) Long recipientId,
            @RequestParam(name = "recipientType", required = false) String recipientType,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1", required = false) int page,
            @RequestParam(name = "limit", defaultValue = "10", required = false) int limit,
            @RequestParam(name = "sortBy", defaultValue = "sentAt", required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = "desc", required = false) String sortOrder) {

        // Validate pagination parameters
        if (page < 1)
            page = 1;
        if (limit < 1 || limit > 100)
            limit = 10;

        // Create sort object
        Sort.Direction direction = sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);

        // Create pageable object
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        return ResponseEntity.ok(notificationService.getEmailNotificationsWithFilters(
                recipientId, recipientType, status, keyword, pageable));
    }

    @GetMapping("/recipient/{recipientId}/{recipientType}")
    public ResponseEntity<List<Notification>> getEmailsByRecipient(
            @PathVariable Long recipientId, @PathVariable String recipientType) {
        List<Notification> emails = notificationService.getEmailNotificationsByRecipient(recipientId, recipientType);
        return ResponseEntity.ok(emails);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEmailStats() {
        Map<String, Object> stats = notificationService.getEmailStatistics();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/compose")
    public ResponseEntity<String> composeEmail(@RequestBody Map<String, Object> request) {
        try {
            String to = (String) request.get("to");
            String subject = (String) request.get("subject");
            String message = (String) request.get("message");
            String templateName = (String) request.get("templateName");
            @SuppressWarnings("unchecked")
            Map<String, Object> variables = (Map<String, Object>) request.get("variables");

            if (templateName != null && !templateName.isEmpty()) {
                emailService.sendTemplateEmail(to, subject, templateName, variables);
            } else {
                emailService.sendSimpleEmail(to, subject, message);
            }

            return ResponseEntity.ok("Email composed and sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to compose email: " + e.getMessage());
        }
    }

    @GetMapping("/templates")
    public ResponseEntity<List<String>> getAvailableTemplates() {
        List<String> templates = List.of(
                "candidate-welcome",
                "interview-invitation",
                "application-rejected",
                "application-accepted",
                "onboarding-welcome");
        return ResponseEntity.ok(templates);
    }
}
