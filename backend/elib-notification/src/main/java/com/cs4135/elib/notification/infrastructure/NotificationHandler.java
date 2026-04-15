package com.cs4135.elib.notification.infrastructure;

import com.cs4135.elib.notification.domain.Notification;
import com.cs4135.elib.notification.domain.NotificationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationHandler.class);

    public Notification handle(Notification notification) {
        try {
            notification.setSentAt(LocalDateTime.now());
            notification.setStatus(NotificationStatus.SENT);

            logger.info(
                    "In-app notification dispatched | notificationId={} | userId={} | referenceId={} | type={} | source={} | message={}",
                    notification.getNotificationId(),
                    notification.getUserId(),
                    notification.getReferenceId(),
                    notification.getType(),
                    notification.getSource(),
                    notification.getMessage()
            );
        } catch (Exception ex) {
            notification.setStatus(NotificationStatus.FAILED);
            logger.error("Notification dispatch failed | notificationId={}", notification.getNotificationId(), ex);
        }

        return notification;
    }
}