package com.example.communications_service.repository;

import com.example.communications_service.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    List<NotificationTemplate> findByTypeAndIsActive(String type, Boolean isActive);

    List<NotificationTemplate> findByIsActive(Boolean isActive);

    NotificationTemplate findByNameAndType(String name, String type);
}


