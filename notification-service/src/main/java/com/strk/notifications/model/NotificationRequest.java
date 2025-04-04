package com.strk.notifications.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotBlank(message = "Message is required")
    private String message;

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Email should be valid")
    private String recipientEmail;

    @NotNull(message = "Notification type is required")
    private Notification.NotificationType type;
}