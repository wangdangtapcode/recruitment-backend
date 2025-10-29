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

        List<Notification> findByRecipientIdAndRecipientType(Long recipientId, String recipientType);

        List<Notification> findByRecipientIdAndRecipientTypeAndIsRead(Long recipientId, String recipientType,
                        boolean isRead);

        List<Notification> findByChannelAndDeliveryStatus(String channel, String deliveryStatus);

        List<Notification> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

        @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.recipientType = :recipientType ORDER BY n.createdAt DESC")
        List<Notification> findRecentNotificationsByRecipient(@Param("recipientId") Long recipientId,
                        @Param("recipientType") String recipientType);

        @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND n.recipientType = :recipientType AND n.isRead = false")
        Long countUnreadNotifications(@Param("recipientId") Long recipientId,
                        @Param("recipientType") String recipientType);

        Page<Notification> findByRecipientIdAndRecipientType(Long recipientId, String recipientType, Pageable pageable);

        Page<Notification> findByChannel(String channel, Pageable pageable);
}
