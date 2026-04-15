package com.cs4135.elib.notification.application.usecases;

import com.cs4135.elib.notification.domain.Notification;
import com.cs4135.elib.notification.domain.NotificationStatus;
import com.cs4135.elib.notification.domain.NotificationType;
import com.cs4135.elib.notification.dto.NotificationRequest;
import com.cs4135.elib.notification.infrastructure.NotificationHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DispatchNotificationUseCase {

    private final NotificationHandler notificationHandler;

    public DispatchNotificationUseCase(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
    }

    public Notification execute(NotificationRequest request) {
        validate(request);

        Notification notification = new Notification(
                UUID.randomUUID(),
                request.getUserId(),
                request.getReferenceId(),
                NotificationType.valueOf(request.getType().trim().toUpperCase()),
                request.getMessage().trim(),
                LocalDateTime.now(),
                null,
                NotificationStatus.PENDING,
                request.getSource() == null || request.getSource().isBlank()
                        ? "UNKNOWN"
                        : request.getSource().trim().toUpperCase()
        );

        return notificationHandler.handle(notification);
    }

    private void validate(NotificationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Notification request must not be null.");
        }

        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required.");
        }

        if (request.getReferenceId() == null) {
            throw new IllegalArgumentException("Reference ID is required.");
        }

        if (request.getType() == null || request.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Notification type is required.");
        }

        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Notification message is required.");
        }

        try {
            NotificationType.valueOf(request.getType().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid notification type. Allowed values: DUE_SOON_REMINDER, OVERDUE_ALERT, BORROW_CONFIRMATION, RETURN_CONFIRMATION."
            );
        }
    }
}