package com.cs4135.elib.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Notification {
    private UUID notificationId;
    private UUID userId;
    private UUID referenceId;
    private NotificationType type;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private NotificationStatus status;
    private String source;
}