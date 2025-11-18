package com.example.communications_service.repository;

import com.example.communications_service.model.MailMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MailMessageRepository extends JpaRepository<MailMessage, Long> {

    Page<MailMessage> findByToUserIdAndToTypeOrderByCreatedAtDesc(Long toUserId, String toType, Pageable pageable);

    Page<MailMessage> findByToUserIdAndToTypeAndDeletedFalseOrderByCreatedAtDesc(Long toUserId, String toType,
            Pageable pageable);

    Page<MailMessage> findByFromUserIdOrderByCreatedAtDesc(Long fromUserId, Pageable pageable);

    Page<MailMessage> findByFromUserIdAndDeletedFalseOrderByCreatedAtDesc(Long fromUserId, Pageable pageable);

    Page<MailMessage> findByThreadIdOrderByCreatedAtAsc(String threadId, Pageable pageable);

    @Query("SELECT m FROM MailMessage m WHERE m.deleted = true AND (m.fromUserId = :userId OR m.toUserId = :userId) ORDER BY m.createdAt DESC")
    Page<MailMessage> findByDeletedTrueAndFromUserIdOrDeletedTrueAndToUserIdOrderByCreatedAtDesc(
            @Param("userId") Long userId1, @Param("userId") Long userId2, Pageable pageable);

    @Query("SELECT m FROM MailMessage m WHERE m.important = true AND m.deleted = false AND (m.fromUserId = :userId OR m.toUserId = :userId) ORDER BY m.createdAt DESC")
    Page<MailMessage> findByImportantTrueAndDeletedFalseAndFromUserIdOrImportantTrueAndDeletedFalseAndToUserIdOrderByCreatedAtDesc(
            @Param("userId") Long userId1, @Param("userId") Long userId2, Pageable pageable);

    @Query("SELECT m FROM MailMessage m WHERE m.starred = true AND m.deleted = false AND (m.fromUserId = :userId OR m.toUserId = :userId) ORDER BY m.createdAt DESC")
    Page<MailMessage> findByStarredTrueAndDeletedFalseAndFromUserIdOrStarredTrueAndDeletedFalseAndToUserIdOrderByCreatedAtDesc(
            @Param("userId") Long userId1, @Param("userId") Long userId2, Pageable pageable);

    Long countByToUserIdAndToTypeAndReadFalseAndDeletedFalse(Long toUserId, String toType);

    boolean existsByGmailMessageId(String gmailMessageId);

    MailMessage findByGmailMessageId(String gmailMessageId);

    @Query("""
            SELECT m FROM MailMessage m WHERE
            (m.fromUserId = :employeeId OR m.toUserId = :employeeId) AND
            (
                (:folder = 'inbox' AND m.toUserId = :employeeId AND m.deleted = false) OR
                (:folder = 'sent' AND m.fromUserId = :employeeId AND m.deleted = false) OR
                (:folder = 'deleted' AND (m.fromUserId = :employeeId OR m.toUserId = :employeeId) AND m.deleted = true) OR
                (:folder = 'important' AND (m.fromUserId = :employeeId OR m.toUserId = :employeeId) AND m.important = true AND m.deleted = false) OR
                (:folder = 'starred' AND (m.fromUserId = :employeeId OR m.toUserId = :employeeId) AND m.starred = true AND m.deleted = false) OR
                ((:folder = 'all' OR :folder IS NULL) AND (m.fromUserId = :employeeId OR m.toUserId = :employeeId) AND m.deleted = false)
            ) AND
            (:read IS NULL OR m.read = :read) AND
            (:important IS NULL OR m.important = :important) AND
            (:starred IS NULL OR m.starred = :starred) AND
            (:external IS NULL OR m.external = :external) AND
            (:keyword IS NULL OR LOWER(m.subject) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<MailMessage> findAllWithFilters(
            @Param("employeeId") Long employeeId,
            @Param("folder") String folder,
            @Param("read") Boolean read,
            @Param("important") Boolean important,
            @Param("starred") Boolean starred,
            @Param("external") Boolean external,
            @Param("keyword") String keyword,
            Pageable pageable);
}
