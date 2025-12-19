package com.example.notification_service.service;

import com.example.notification_service.dto.Meta;
import com.example.notification_service.dto.PaginationDTO;
import com.example.notification_service.dto.notification.BulkNotificationRequest;
import com.example.notification_service.dto.notification.NotificationPayload;
import com.example.notification_service.exception.NotificationNotFoundException;
import com.example.notification_service.model.Notification;
import com.example.notification_service.repository.NotificationRepository;
import com.example.notification_service.messaging.NotificationEvent;
import com.example.notification_service.utils.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final SocketIOBroadcastService socketIOBroadcastService;

    public NotificationService(NotificationRepository notificationRepository,
            UserService userService,
            SocketIOBroadcastService socketIOBroadcastService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.socketIOBroadcastService = socketIOBroadcastService;
    }

    public Notification createNotification(Long recipientId,
            String title,
            String message) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setSentAt(LocalDateTime.now());
        notification.setDeliveryStatus("SENT");
        Notification saved = notificationRepository.save(notification);
        publishNotification(saved, "NOTIFICATION_CREATED");
        return saved;
    }

    public List<Notification> getNotificationsByRecipient(Long recipientId) {
        return notificationRepository.findByRecipientId(recipientId);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            Notification updated = notificationRepository.save(notification);
            publishNotification(updated, "NOTIFICATION_READ");
            long unread = notificationRepository.countByRecipientIdAndIsReadFalse(updated.getRecipientId());
            socketIOBroadcastService.publishUnreadCount(updated.getRecipientId(), unread);
        }
    }

    public PaginationDTO getAllNotificationsWithFilters(Long recipientId, String status, Pageable pageable) {

        Page<Notification> notificationPage;

        if (recipientId != null) {
            notificationPage = notificationRepository.findByRecipientId(recipientId, pageable);
        } else if (status != null) {
            notificationPage = notificationRepository.findByDeliveryStatus(status, pageable);
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

    public Map<String, Object> getNotificationStats(Long recipientId) {
        long total = notificationRepository.count();
        long unread = recipientId != null
                ? notificationRepository.countByRecipientIdAndIsReadFalse(recipientId)
                : notificationRepository.countByIsReadFalse();

        return Map.of(
                "totalNotifications", total,
                "unreadNotifications", unread);
    }

    public void processNotificationEvent(NotificationEvent event) {
        List<Long> recipients = resolveRecipients(event);
        System.out.println("Recipients: " + recipients);
        if (recipients.isEmpty()) {
            return;
        }
        recipients.forEach(id -> createNotification(id, event.getTitle(), event.getMessage()));
    }

    private List<Long> resolveRecipients(NotificationEvent event) {
        Set<Long> recipientSet = new HashSet<>();

        if (Boolean.TRUE.equals(event.getIncludeAllEmployees())) {
            recipientSet.addAll(userService.getAllEmployeeIds(event.getAuthToken()));
        }
        System.out.println("event.getDepartmentId(): " + event.getDepartmentId());
        System.out.println("event.getPositionId(): " + event.getPositionId());
        if (event.getDepartmentId() != null) {
            System.out.println("vào đây");
            recipientSet.addAll(userService.getEmployeeIdsByFilters(event.getDepartmentId(), event.getPositionId(),
                    event.getAuthToken()));
        } else if (event.getPositionId() != null) {
            System.out.println("vào đây 2");
            recipientSet.addAll(userService.getEmployeeIdsByFilters(null, event.getPositionId(), event.getAuthToken()));
        }

        if (event.getRecipientIds() != null) {
            recipientSet.addAll(event.getRecipientIds());
        }

        if (event.getRecipientId() != null) {
            recipientSet.add(event.getRecipientId());
        }

        return recipientSet.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Gửi thông báo hàng loạt theo các điều kiện
     * Hỗ trợ gửi cho: tất cả nhân viên công ty, tất cả nhân viên phòng ban,
     * nhân viên theo vị trí, status, keyword, hoặc danh sách cụ thể
     * 
     * @param request BulkNotificationRequest chứa thông tin thông báo và điều kiện
     * @return Số lượng thông báo đã được tạo
     */
    public int createBulkNotificationsByConditions(BulkNotificationRequest request) {
        List<Long> recipientIds = resolveRecipientsFromRequest(request);

        if (recipientIds.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Long recipientId : recipientIds) {
            createNotification(recipientId, request.getTitle(), request.getMessage());
            count++;
        }

        return count;
    }

    /**
     * Xử lý và lấy danh sách recipient IDs từ BulkNotificationRequest
     * 
     * @param request BulkNotificationRequest
     * @return Danh sách recipient IDs
     */
    private List<Long> resolveRecipientsFromRequest(BulkNotificationRequest request) {
        Set<Long> recipientSet = new HashSet<>();
        String authToken = SecurityUtil.getCurrentUserJWT().orElse(null);

        // Ưu tiên 1: Gửi cho tất cả nhân viên công ty
        if (Boolean.TRUE.equals(request.getIncludeAllEmployees())) {
            recipientSet.addAll(userService.getAllEmployeeIds(authToken));
        }
        // Ưu tiên 2: Gửi cho nhân viên theo điều kiện (department, position, status,
        // keyword)
        else if (request.getDepartmentId() != null || request.getPositionId() != null
                || (request.getStatus() != null && !request.getStatus().isEmpty())
                || (request.getKeyword() != null && !request.getKeyword().isEmpty())) {
            recipientSet.addAll(userService.getEmployeeIdsByFilters(
                    request.getDepartmentId(),
                    request.getPositionId(),
                    request.getStatus(),
                    request.getKeyword(),
                    authToken));
        }

        // Ưu tiên 3: Gửi cho danh sách nhân viên cụ thể
        if (request.getRecipientIds() != null && !request.getRecipientIds().isEmpty()) {
            recipientSet.addAll(request.getRecipientIds());
        }

        // Ưu tiên 4: Gửi cho một nhân viên cụ thể
        if (request.getRecipientId() != null) {
            recipientSet.add(request.getRecipientId());
        }

        return recipientSet.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private void publishNotification(Notification notification, String eventType) {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🔄 PUBLISHING NOTIFICATION");
        System.out.println("   Notification ID: " + notification.getId());
        System.out.println("   Recipient ID    : " + notification.getRecipientId());
        System.out.println("   Title           : " + notification.getTitle());
        System.out.println("   Event Type     : " + eventType);
        System.out.println("═══════════════════════════════════════════════════════════");

        NotificationPayload payload = NotificationPayload.from(notification, eventType);
        socketIOBroadcastService.pushNotification(payload);
    }
}
