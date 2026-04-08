package com.cs4135.elib.notification.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class NotificationRequest {
    private UUID userId;
    private String type;
    private String message;
}
