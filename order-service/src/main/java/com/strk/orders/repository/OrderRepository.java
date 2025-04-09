package com.strk.orders.repository;

import java.util.UUID;

import com.strk.orders.model.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, UUID>, CustomOrderRepository {
}