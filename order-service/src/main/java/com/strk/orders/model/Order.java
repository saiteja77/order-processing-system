package com.strk.orders.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.strk.common.model.OrderStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("orders")
public class Order implements Persistable<UUID> {

    @Id
    @With
    private UUID id;
    private String customerName;
    private Double totalAmount;
    private OrderStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    private boolean isNew = true;

    @Override
    @Transient
    public boolean isNew() {
        return isNew;
    }

    /**
     * @author saiteja77
     * Creates Order entity Object which is to be persisted
     * @param customerName - Customer Name
     * @param totalAmount - Total amount
     * @param description - Description
     * @return Order
     */
    public static Order createNewOrder(String customerName, Double totalAmount, String description) {
        return Order.builder()
                .customerName(customerName)
                .totalAmount(totalAmount)
                .description(description)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isNew(true)
                .build();
    }
}