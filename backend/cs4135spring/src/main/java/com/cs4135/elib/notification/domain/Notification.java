package com.cs4135.elib.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Notification {
    private UUID userId;
    private NotificationType type;
    private String message;
}