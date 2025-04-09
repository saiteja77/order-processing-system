package com.strk.orders.repository;

import com.strk.orders.model.Order;
import reactor.core.publisher.Mono;

public interface CustomOrderRepository {
    Mono<Integer> update(Order order);
}
