package com.example.communications_service.service;

import com.example.communications_service.dto.Meta;
import com.example.communications_service.dto.PaginationDTO;
import com.example.communications_service.model.Notification;
import com.example.communications_service.model.NotificationTemplate;
import com.example.communications_service.repository.NotificationRepository;
import com.example.communications_service.repository.NotificationTemplateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    public NotificationService(NotificationRepository notificationRepository,
            NotificationTemplateRepository notificationTemplateRepository, EmailService emailService,
            SmsService smsService) {
        this.notificationRepository = notificationRepository;
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    public Notification createNotification(Long recipientId, String recipientType, String channel,
            String title, String message) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setRecipientType(recipientType);
        notification.setChannel(channel);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(channel);

        return notificationRepository.save(notification);
    }

    public Notification createNotificationFromTemplate(Long recipientId, String recipientType,
            String channel, Long templateId,
            Map<String, Object> variables) {
        Optional<NotificationTemplate> templateOpt = notificationTemplateRepository.findById(templateId);
        if (templateOpt.isEmpty()) {
            throw new RuntimeException("Template not found with id: " + templateId);
        }

        NotificationTemplate template = templateOpt.get();
        String processedTitle = processTemplate(template.getSubject(), variables);
        String processedMessage = processTemplate(template.getContent(), variables);

        Notification notification = createNotification(recipientId, recipientType, channel,
                processedTitle, processedMessage);
        notification.setTemplate(template);

        return notification;
    }

    public void sendNotification(Notification notification) {
        try {
            switch (notification.getChannel()) {
                case "EMAIL":
                    emailService.sendSimpleEmail(
                            getRecipientEmail(notification.getRecipientId(), notification.getRecipientType()),
                            notification.getTitle(),
                            notification.getMessage());
                    break;
                case "SMS":
                    smsService.sendSms(
                            getRecipientPhone(notification.getRecipientId(), notification.getRecipientType()),
                            notification.getMessage());
                    break;
                default:
                    // For IN_APP notifications, just mark as sent
                    break;
            }

            notification.setSentAt(LocalDateTime.now());
            notification.setDeliveryStatus("SENT");
            notification.setDelivered(true);

        } catch (Exception e) {
            notification.setDeliveryStatus("FAILED");
            notification.setErrorMessage(e.getMessage());
        }

        notificationRepository.save(notification);
    }

    public void sendBulkNotifications(List<Notification> notifications) {
        for (Notification notification : notifications) {
            sendNotification(notification);
        }
    }

    public List<Notification> getNotificationsByRecipient(Long recipientId, String recipientType) {
        return notificationRepository.findByRecipientIdAndRecipientType(recipientId, recipientType);
    }

    public void markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    private String processTemplate(String template, Map<String, Object> variables) {
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return result;
    }

    private String getRecipientEmail(Long recipientId, String recipientType) {
        // TODO: Implement logic to get email from user-service or candidate-service
        // For USER: Call user-service API to get employee email
        // For CANDIDATE: Call candidate-service API to get candidate email
        // This would typically involve a REST call to the respective service
        // Currently returns placeholder - should be implemented when user/candidate
        // services provide email endpoints
        return "recipient@example.com";
    }

    private String getRecipientPhone(Long recipientId, String recipientType) {
        // TODO: Implement logic to get phone from user-service or candidate-service
        // For USER: Call user-service API to get employee phone
        // For CANDIDATE: Call candidate-service API to get candidate phone
        // Currently returns placeholder - should be implemented when user/candidate
        // services provide phone endpoints
        return "+84901234567";
    }

    public PaginationDTO getAllNotificationsWithFilters(Long recipientId, String recipientType,
            String channel, String status, String keyword, Pageable pageable) {

        Page<Notification> notificationPage;

        if (recipientId != null && recipientType != null) {
            notificationPage = notificationRepository.findByRecipientIdAndRecipientType(recipientId, recipientType,
                    pageable);
        } else if (channel != null) {
            notificationPage = notificationRepository.findByChannel(channel, pageable);
        } else {
            notificationPage = notificationRepository.findAll(pageable);
        }

        Meta meta = new Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(notificationPage.getTotalElements());
        meta.setPages(notificationPage.getTotalPages());

        PaginationDTO dto = new PaginationDTO();
        dto.setResult(notificationPage.getContent());
        dto.setMeta(meta);
        return dto;
    }

    public PaginationDTO getEmailNotificationsWithFilters(Long recipientId, String recipientType,
            String status, String keyword, Pageable pageable) {

        Page<Notification> notificationPage = notificationRepository.findByChannel("EMAIL", pageable);

        Meta meta = new Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(notificationPage.getTotalElements());
        meta.setPages(notificationPage.getTotalPages());

        PaginationDTO dto = new PaginationDTO();
        dto.setResult(notificationPage.getContent());
        dto.setMeta(meta);
        return dto;
    }

    public List<Notification> getEmailNotificationsByRecipient(Long recipientId, String recipientType) {
        return notificationRepository.findByRecipientIdAndRecipientType(recipientId, recipientType)
                .stream()
                .filter(n -> "EMAIL".equals(n.getChannel()))
                .toList();
    }

    public Map<String, Object> getEmailStatistics() {
        long totalEmails = notificationRepository.count();
        long sentEmails = notificationRepository.findByChannelAndDeliveryStatus("EMAIL", "SENT").size();
        long failedEmails = notificationRepository.findByChannelAndDeliveryStatus("EMAIL", "FAILED").size();
        long pendingEmails = notificationRepository.findByChannelAndDeliveryStatus("EMAIL", "PENDING").size();

        return Map.of(
                "totalEmails", totalEmails,
                "sentEmails", sentEmails,
                "failedEmails", failedEmails,
                "pendingEmails", pendingEmails,
                "successRate", totalEmails > 0 ? (double) sentEmails / totalEmails * 100 : 0);
    }
}
