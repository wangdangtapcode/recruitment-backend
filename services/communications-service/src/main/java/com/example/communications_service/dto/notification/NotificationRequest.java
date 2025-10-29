package com.example.communications_service.dto.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class NotificationRequest {

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotBlank(message = "Recipient type is required")
    private String recipientType; // "USER" or "CANDIDATE"

    @NotBlank(message = "Channel is required")
    private String channel; // "EMAIL", "SMS", "IN_APP"

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private String priority = "NORMAL"; // "LOW", "NORMAL", "HIGH", "URGENT"

    private Long templateId;

    private Map<String, Object> variables;
}


