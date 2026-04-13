package com.cs4135.elib.notification.application.usecases;

import com.cs4135.elib.notification.domain.Notification;
import com.cs4135.elib.notification.domain.NotificationType;
import com.cs4135.elib.notification.dto.NotificationRequest;
import com.cs4135.elib.notification.infrastructure.NotificationHandler;
import org.springframework.stereotype.Component;

@Component
public class SendNotificationUseCase {

    private final NotificationHandler notificationHandler;

    public SendNotificationUseCase(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
    }

    public void execute(NotificationRequest request) {
        validate(request);

        NotificationType type = NotificationType.valueOf(request.getType().trim().toUpperCase());

        Notification notification = new Notification(
                request.getUserId(),
                type,
                request.getMessage().trim()
        );

        notificationHandler.handle(notification);
    }

    private void validate(NotificationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Notification request must not be null.");
        }

        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required.");
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
            throw new IllegalArgumentException("Invalid notification type. Allowed values: REMINDER, OVERDUE.");
        }
    }
}