package com.strk.notifications.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import com.strk.common.model.OrderEvent;
import com.strk.common.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.strk.notifications.service.NotificationService;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @Test
    void getNotifications_ReturnsEvents() {
        // Given
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        OrderEvent event1 = createOrderEvent(orderId1, "Customer1", 100.0, OrderStatus.CREATED);
        OrderEvent event2 = createOrderEvent(orderId2, "Customer2", 200.0, OrderStatus.PROCESSING);

        when(notificationService.getNotifications()).thenReturn(Flux.just(event1, event2));

        // When
        WebTestClient webTestClient = WebTestClient.bindToController(notificationController).build();

        // Then
        webTestClient.get()
                .uri("/notifications")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(OrderEvent.class)
                .hasSize(2)
                .contains(event1, event2);

        verify(notificationService, times(1)).getNotifications();
    }

    @Test
    void getNotifications_EmptyList() {
        // Given
        when(notificationService.getNotifications()).thenReturn(Flux.empty());

        // When
        WebTestClient webTestClient = WebTestClient.bindToController(notificationController).build();

        // Then
        webTestClient.get()
                .uri("/notifications")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(OrderEvent.class)
                .hasSize(0);

        verify(notificationService, times(1)).getNotifications();
    }

    @Test
    void getNotifications_FluxTest() {
        // Given
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        OrderEvent event1 = createOrderEvent(orderId1, "Customer1", 100.0, OrderStatus.CREATED);
        OrderEvent event2 = createOrderEvent(orderId2, "Customer2", 200.0, OrderStatus.PROCESSING);

        when(notificationService.getNotifications()).thenReturn(Flux.just(event1, event2));

        // When
        Flux<OrderEvent> result = notificationController.getNotifications();

        // Then
        StepVerifier.create(result)
                .expectNext(event1)
                .expectNext(event2)
                .verifyComplete();

        verify(notificationService, times(1)).getNotifications();
    }

    private OrderEvent createOrderEvent(UUID orderId, String customerName, Double totalAmount, OrderStatus status) {
        return OrderEvent.builder()
                .orderId(orderId)
                .customerName(customerName)
                .totalAmount(totalAmount)
                .status(status)
                .description("Test event")
                .timestamp(LocalDateTime.now())
                .eventType(OrderEvent.EventType.CREATED)
                .build();
    }
}