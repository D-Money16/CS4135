package com.cs4135.elib.notification.domain;

import java.time.LocalDateTime;
import java.util.UUID;

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

    public Notification() {
    }

    public Notification(UUID notificationId,
                        UUID userId,
                        UUID referenceId,
                        NotificationType type,
                        String message,
                        LocalDateTime createdAt,
                        LocalDateTime sentAt,
                        NotificationStatus status,
                        String source) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.referenceId = referenceId;
        this.type = type;
        this.message = message;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.status = status;
        this.source = source;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}