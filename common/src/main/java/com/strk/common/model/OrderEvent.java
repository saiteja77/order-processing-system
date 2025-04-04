package com.strk.common.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private UUID orderId;
    private String customerName;
    private Double totalAmount;
    private OrderStatus status;
    private String description;
    private LocalDateTime timestamp;
    private EventType eventType;

    public enum EventType {
        CREATED,
        UPDATED
    }

    @Override
    public String toString() {
        return "OrderEvent{" +
                "orderId=" + orderId +
                ", customerName='" + customerName + '\'' +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                ", eventType=" + eventType +
                '}';
    }
}
