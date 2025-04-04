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
public class NotificationResponse {
    private UUID id;
    private UUID orderId;
    private String message;
    private Notification.NotificationType type;
    private LocalDateTime sentAt;
    private String status;

    public static NotificationResponse fromNotification(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .orderId(notification.getOrderId())
                .message(notification.getMessage())
                .type(notification.getType())
                .sentAt(notification.getSentAt())
                .status("SENT")
                .build();
    }
}