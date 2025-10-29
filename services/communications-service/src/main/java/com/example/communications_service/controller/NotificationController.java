package com.example.communications_service.controller;

import com.example.communications_service.dto.notification.NotificationRequest;
import com.example.communications_service.model.Notification;
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
@RequestMapping("/api/v1/communications-service/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(@Valid @RequestBody NotificationRequest request) {
        Notification notification = notificationService.createNotification(
                request.getRecipientId(),
                request.getRecipientType(),
                request.getChannel(),
                request.getTitle(),
                request.getMessage(),
                request.getPriority());

        // Send notification immediately
        notificationService.sendNotification(notification);

        return ResponseEntity.ok(notification);
    }

    @PostMapping("/template")
    public ResponseEntity<Notification> createNotificationFromTemplate(
            @Valid @RequestBody NotificationRequest request) {
        Notification notification = notificationService.createNotificationFromTemplate(
                request.getRecipientId(),
                request.getRecipientType(),
                request.getChannel(),
                request.getTemplateId(),
                request.getVariables(),
                request.getPriority());

        // Send notification immediately
        notificationService.sendNotification(notification);

        return ResponseEntity.ok(notification);
    }

    @PostMapping("/bulk")
    public ResponseEntity<String> sendBulkNotifications(@Valid @RequestBody List<NotificationRequest> requests) {
        for (NotificationRequest request : requests) {
            Notification notification = notificationService.createNotification(
                    request.getRecipientId(),
                    request.getRecipientType(),
                    request.getChannel(),
                    request.getTitle(),
                    request.getMessage(),
                    request.getPriority());
            notificationService.sendNotification(notification);
        }

        return ResponseEntity.ok("Bulk notifications sent successfully");
    }

    // Unified GET endpoint for all notifications with filtering, pagination, and
    // sorting
    @GetMapping
    public ResponseEntity<com.example.communications_service.dto.PaginationDTO> getAllNotifications(
            @RequestParam(name = "recipientId", required = false) Long recipientId,
            @RequestParam(name = "recipientType", required = false) String recipientType,
            @RequestParam(name = "channel", required = false) String channel,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1", required = false) int page,
            @RequestParam(name = "limit", defaultValue = "10", required = false) int limit,
            @RequestParam(name = "sortBy", defaultValue = "id", required = false) String sortBy,
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

        return ResponseEntity.ok(notificationService.getAllNotificationsWithFilters(
                recipientId, recipientType, channel, status, keyword, pageable));
    }

    @GetMapping("/recipient/{recipientId}/{recipientType}")
    public ResponseEntity<List<Notification>> getNotificationsByRecipient(
            @PathVariable Long recipientId,
            @PathVariable String recipientType) {
        List<Notification> notifications = notificationService.getNotificationsByRecipient(recipientId, recipientType);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read");
    }

    @GetMapping("/stats/{recipientId}/{recipientType}")
    public ResponseEntity<Map<String, Object>> getNotificationStats(
            @PathVariable Long recipientId,
            @PathVariable String recipientType) {
        // This would typically return statistics like unread count, etc.
        return ResponseEntity.ok(Map.of("message", "Stats endpoint - to be implemented"));
    }
}
