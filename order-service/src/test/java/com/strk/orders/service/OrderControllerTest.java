package com.strk.orders.service;

import java.util.UUID;

import com.strk.common.exception.GlobalExceptionHandler;
import com.strk.common.model.OrderStatus;
import com.strk.orders.controllers.OrderController;
import com.strk.common.exception.OrderNotFoundException;
import com.strk.orders.model.OrderRequest;
import com.strk.orders.model.OrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
public class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @Test
    void createOrder_Success() {
        // Given
        OrderRequest request = OrderRequest.builder()
                .customerName("Test Customer")
                .totalAmount(100.0)
                .description("Test Order")
                .build();

        OrderResponse response = OrderResponse.builder()
                .id(UUID.randomUUID())
                .customerName("Test Customer")
                .totalAmount(100.0)
                .description("Test Order")
                .status(OrderStatus.CREATED)
                .build();

        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(Mono.just(response));

        // When & Then
        webTestClient.post()
                .uri("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponse.class)
                .isEqualTo(response);
    }

    @Test
    void getOrderById_Success() {
        // Given
        UUID orderId = UUID.randomUUID();
        OrderResponse response = OrderResponse.builder()
                .id(orderId)
                .customerName("Test Customer")
                .totalAmount(100.0)
                .description("Test Order")
                .status(OrderStatus.CREATED)
                .build();

        when(orderService.getOrderById(orderId)).thenReturn(Mono.just(response));

        // When & Then
        webTestClient.get()
                .uri("/orders/{id}", orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponse.class)
                .isEqualTo(response);
    }

    @Test
    void getOrderById_NotFound() {
        // Given
        UUID orderId = UUID.randomUUID();
        when(orderService.getOrderById(orderId)).thenReturn(Mono.error(new OrderNotFoundException("Order not found")));

        // When & Then
        webTestClient.get()
                .uri("/orders/{id}", orderId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateOrder_Success() {
        // Given
        UUID orderId = UUID.randomUUID();
        OrderRequest request = OrderRequest.builder()
                .customerName("Updated Customer")
                .totalAmount(150.0)
                .description("Updated Order")
                .build();

        OrderResponse response = OrderResponse.builder()
                .id(orderId)
                .customerName("Updated Customer")
                .totalAmount(150.0)
                .description("Updated Order")
                .status(OrderStatus.CREATED)
                .build();

        when(orderService.updateOrder(eq(orderId), any(OrderRequest.class))).thenReturn(Mono.just(response));

        // When & Then
        webTestClient.put()
                .uri("/orders/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponse.class)
                .isEqualTo(response);
    }

    @Test
    void updateOrder_NotFound() {
        // Given
        UUID orderId = UUID.randomUUID();
        OrderRequest request = OrderRequest.builder()
                .customerName("Updated Customer")
                .totalAmount(150.0)
                .description("Updated Order")
                .build();

        when(orderService.updateOrder(eq(orderId), any(OrderRequest.class)))
                .thenReturn(Mono.error(new OrderNotFoundException("Order not found")));

        // When & Then
        webTestClient.put()
                .uri("/orders/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }
}