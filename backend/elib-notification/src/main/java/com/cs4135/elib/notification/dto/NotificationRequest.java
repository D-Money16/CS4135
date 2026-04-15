package com.cs4135.elib.notification.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class NotificationRequest {
    private UUID userId;
    private UUID referenceId;
    private String type;
    private String message;
    private String source;
}