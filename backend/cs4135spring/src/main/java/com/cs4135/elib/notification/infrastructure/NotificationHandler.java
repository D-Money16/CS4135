package com.cs4135.elib.notification.infrastructure;

import com.cs4135.elib.notification.domain.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationHandler.class);

    public void handle(Notification notification) {
        logger.info(
                "Notification processed | userId={} | type={} | message={}",
                notification.getUserId(),
                notification.getType(),
                notification.getMessage()
        );
    }
}