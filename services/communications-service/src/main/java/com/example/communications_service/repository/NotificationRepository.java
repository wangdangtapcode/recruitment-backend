package com.example.communications_service.repository;

import com.example.communications_service.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

        List<Notification> findByRecipientId(Long recipientId);

        List<Notification> findByRecipientIdAndIsRead(Long recipientId, boolean isRead);

        List<Notification> findByDeliveryStatus(String deliveryStatus);

        List<Notification> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

        @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId ORDER BY n.createdAt DESC")
        List<Notification> findRecentNotificationsByRecipient(@Param("recipientId") Long recipientId);

        @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND n.isRead = false")
        Long countUnreadNotifications(@Param("recipientId") Long recipientId);

        Long countByRecipientIdAndIsReadFalse(Long recipientId);

        Long countByIsReadFalse();

        Page<Notification> findByRecipientId(Long recipientId, Pageable pageable);

        Page<Notification> findByDeliveryStatus(String deliveryStatus, Pageable pageable);
}
