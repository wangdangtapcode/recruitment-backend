package com.example.communications_service.service;

import com.example.communications_service.dto.Meta;
import com.example.communications_service.dto.PaginationDTO;
import com.example.communications_service.model.Notification;
import com.example.communications_service.repository.NotificationRepository;
import com.example.communications_service.messaging.NotificationEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationService(NotificationRepository notificationRepository,
            UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    public Notification createNotification(Long recipientId,
            String title,
            String message) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setTitle(title);
        notification.setMessage(message);
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByRecipient(Long recipientId) {
        return notificationRepository.findByRecipientId(recipientId);
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
}
