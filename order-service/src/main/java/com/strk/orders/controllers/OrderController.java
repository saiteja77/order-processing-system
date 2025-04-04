package com.strk.orders.controllers;
import java.util.UUID;

import com.strk.orders.model.OrderRequest;
import com.strk.orders.model.OrderResponse;
import com.strk.orders.service.OrderService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * @author saiteja77
     * @param orderRequest - contains the user's request body with order details
     * @return OrderResponse
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }

    /**
     * @author saiteja77
     * @param id - contains the order id to retrieve the order
     * @return OrderResponse
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<OrderResponse>> getOrderById(@PathVariable UUID id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * @author saiteja77
     * @param id - Order id to update
     * @param orderRequest - Order details to be updated
     * @return OrderResponse
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<OrderResponse>> updateOrder(
            @PathVariable UUID id,
            @Valid @RequestBody OrderRequest orderRequest) {
        return orderService.updateOrder(id, orderRequest)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}