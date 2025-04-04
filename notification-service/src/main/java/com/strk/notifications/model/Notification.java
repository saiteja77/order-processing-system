package com.strk.notifications.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private UUID id;
    private UUID orderId;
    private String message;
    private String recipientEmail;
    private NotificationType type;
    private LocalDateTime sentAt;

    public enum NotificationType {
        ORDER_CREATED,
        ORDER_UPDATED,
        ORDER_SHIPPED,
        ORDER_DELIVERED
    }
}