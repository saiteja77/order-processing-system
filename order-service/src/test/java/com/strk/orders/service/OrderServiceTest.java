package com.strk.orders.service;

import java.time.LocalDateTime;
import java.util.UUID;

import com.strk.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.strk.common.model.OrderEvent;
import com.strk.common.model.OrderStatus;
import com.strk.common.exception.OrderNotFoundException;
import com.strk.orders.model.Order;
import com.strk.orders.model.OrderRequest;
import com.strk.orders.repository.OrderRepository;

import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest orderRequest;
    private Order order;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();

        orderRequest = OrderRequest.builder()
                .customerName("Test Customer")
                .totalAmount(100.0)
                .description("Test Order")
                .build();

        order = Order.builder()
                .id(orderId)
                .customerName("Test Customer")
                .totalAmount(100.0)
                .description("Test Order")
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createOrder_Success() {
        // Given
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(kafkaProducerService.sendOrderEvent(any(OrderEvent.class)))
                .thenReturn(Mono.just(createMock(SenderResult.class)));

        // When & Then
        StepVerifier.create(orderService.createOrder(orderRequest))
                .expectNextMatches(orderResponse ->
                        orderResponse.getCustomerName().equals("Test Customer") &&
                                orderResponse.getTotalAmount().equals(100.0) &&
                                orderResponse.getStatus().equals(OrderStatus.CREATED)
                )
                .verifyComplete();
    }

    @Test
    void getOrderById_Success() {
        // Given
        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));

        // When & Then
        StepVerifier.create(orderService.getOrderById(orderId))
                .expectNextMatches(orderResponse ->
                        orderResponse.getId().equals(orderId) &&
                                orderResponse.getCustomerName().equals("Test Customer")
                )
                .verifyComplete();
    }

    @Test
    void getOrderById_NotFound() {
        // Given
        when(orderRepository.findById(orderId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(orderService.getOrderById(orderId))
                .expectError(OrderNotFoundException.class)
                .verify();
    }

    @Test
    void updateOrder_Success() {
        // Given
        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(kafkaProducerService.sendOrderEvent(any(OrderEvent.class)))
                .thenReturn(Mono.just(createMock(SenderResult.class)));

        // When & Then
        StepVerifier.create(orderService.updateOrder(orderId, orderRequest))
                .expectNextMatches(orderResponse ->
                        orderResponse.getId().equals(orderId) &&
                                orderResponse.getCustomerName().equals("Test Customer")
                )
                .verifyComplete();
    }

    @Test
    void updateOrder_NotFound() {
        // Given
        when(orderRepository.findById(orderId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(orderService.updateOrder(orderId, orderRequest))
                .expectError(OrderNotFoundException.class)
                .verify();
    }

    private <T> T createMock(Class<T> clazz) {
        return Mockito.mock(clazz);
    }
}