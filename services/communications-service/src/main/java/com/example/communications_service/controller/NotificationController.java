package com.example.communications_service.controller;

import com.example.communications_service.dto.notification.NotificationRequest;
import com.example.communications_service.model.Notification;
import com.example.communications_service.service.NotificationService;
import com.example.communications_service.utils.SecurityUtil;
import com.example.communications_service.utils.annotation.ApiMessage;

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
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(@Valid @RequestBody NotificationRequest request) {
        Notification notification = notificationService.createNotification(
                request.getRecipientId(),
                request.getTitle(),
                request.getMessage());
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/bulk")
    public ResponseEntity<String> createBulkNotifications(@Valid @RequestBody List<NotificationRequest> requests) {
        requests.forEach(req -> notificationService.createNotification(
                req.getRecipientId(),
                req.getTitle(),
                req.getMessage()));
        return ResponseEntity.ok("Notifications created successfully");
    }

    // Unified GET endpoint for all notifications with filtering, pagination, and
    // sorting
    @GetMapping
    @ApiMessage("Lấy danh sách thông báo")
    public ResponseEntity<com.example.communications_service.dto.PaginationDTO> getAllNotifications(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "1", required = false) int page,
            @RequestParam(name = "limit", defaultValue = "10", required = false) int limit,
            @RequestParam(name = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = "desc", required = false) String sortOrder) {

        // Validate pagination parameters
        if (page < 1)
            page = 1;
        if (limit < 1 || limit > 100)
            limit = 10;
        Long recipientId = SecurityUtil.extractEmployeeId();
        // Create sort object
        Sort.Direction direction = sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);

        // Create pageable object
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        return ResponseEntity.ok(notificationService.getAllNotificationsWithFilters(
                recipientId, status, pageable));
    }

    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<List<Notification>> getNotificationsByRecipient(
            @PathVariable Long recipientId) {
        List<Notification> notifications = notificationService.getNotificationsByRecipient(recipientId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read");
    }

    @GetMapping("/stats/{recipientId}")
    public ResponseEntity<Map<String, Object>> getNotificationStats(
            @PathVariable Long recipientId) {
        return ResponseEntity.ok(notificationService.getNotificationStats(recipientId));
    }
}
