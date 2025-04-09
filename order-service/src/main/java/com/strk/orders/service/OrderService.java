package com.strk.orders.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.strk.common.model.OrderEvent;
import com.strk.common.exception.OrderNotFoundException;
import com.strk.orders.model.Order;
import com.strk.orders.model.OrderRequest;
import com.strk.orders.model.OrderResponse;
import com.strk.orders.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaProducerService kafkaProducerService;

    /**
     * @author saiteja77
     * Persists the new order into the database and sensd the order creation event to Kafka
     * @param orderRequest - contains the user's request body with order details
     * @return OrderResponse
     */
    @Transactional
    public Mono<OrderResponse> createOrder(OrderRequest orderRequest) {
        Order order = Order.createNewOrder(
                orderRequest.getCustomerName(),
                orderRequest.getTotalAmount(),
                orderRequest.getDescription()
        );

        return orderRepository.save(order)
                .flatMap(savedOrder -> {
                    OrderEvent orderEvent = buildOrderEvent(savedOrder, OrderEvent.EventType.CREATED);

                    return kafkaProducerService.sendOrderEvent(orderEvent)
                            .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                                    .doBeforeRetry(retrySignal ->
                                            log.warn("Retrying Kafka send for order {}, attempt {}",
                                                    savedOrder.getId(), retrySignal.totalRetries() + 1)))
                            .onErrorResume(e -> {
                                log.error("Failed to send order event to Kafka after retries: {}", e.getMessage());
                                // Proceed anyway to not block the API response, but log the error
                                return Mono.empty();
                            })
                            .thenReturn(savedOrder);
                })
                .map(OrderResponse::fromOrder);
    }

    /**
     * @author saiteja77
     * @param id - contains the order id to retrieve the order
     * @return OrderResponse
     */
    public Mono<OrderResponse> getOrderById(UUID id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("Order not found with id: " + id)))
                .map(OrderResponse::fromOrder);
    }

    /**
     * @author saiteja77
     * Updates the existing order into the database and sensd the order updation event to Kafka
     * @param id - Order id to update
     * @param orderRequest - Order details to be updated
     * @return OrderResponse
     */
    @Transactional
    public Mono<OrderResponse> updateOrder(UUID id, OrderRequest orderRequest) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("Order not found with id: " + id)))
                .flatMap(existingOrder -> {
                    existingOrder.setCustomerName(orderRequest.getCustomerName());
                    existingOrder.setTotalAmount(orderRequest.getTotalAmount());
                    existingOrder.setDescription(orderRequest.getDescription());
                    existingOrder.setUpdatedAt(LocalDateTime.now());

                    return orderRepository.update(existingOrder)
                            .flatMap(numOfOrdersUpdated -> {
                                OrderEvent orderEvent = buildOrderEvent(existingOrder, OrderEvent.EventType.UPDATED);

                                return kafkaProducerService.sendOrderEvent(orderEvent)
                                        .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                                                .doBeforeRetry(retrySignal ->
                                                        log.warn("Retrying Kafka send for order update {}, attempt {}",
                                                                existingOrder.getId(), retrySignal.totalRetries() + 1)))
                                        .onErrorResume(e -> {
                                            log.error("Failed to send order update event to Kafka after retries: {}", e.getMessage());
                                            return Mono.empty();
                                        })
                                        .thenReturn(existingOrder);
                            });
                })
                .map(OrderResponse::fromOrder);
    }

    private OrderEvent buildOrderEvent(Order order, OrderEvent.EventType eventType) {
        return OrderEvent.builder()
                .orderId(order.getId())
                .customerName(order.getCustomerName())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .description(order.getDescription())
                .timestamp(LocalDateTime.now())
                .eventType(eventType)
                .build();
    }
}