package com.strk.orders.repository;

import com.strk.orders.model.Order;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class CustomOrderRepositoryImpl implements CustomOrderRepository {

    private final DatabaseClient databaseClient;

    public CustomOrderRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Integer> update(Order order) {
        return databaseClient.sql("""
                UPDATE orders SET
                    customer_name = :customer_name,
                    description = :description,
                    status = :status,
                    total_amount = :totalAmount,
                    updated_at = :updatedAt
                WHERE id = :id
                """)
                .bind("customer_name", order.getCustomerName())
                .bind("description", order.getDescription())
                .bind("status", order.getStatus().toString())
                .bind("totalAmount", order.getTotalAmount())
                .bind("updatedAt", order.getUpdatedAt())
                .bind("id", order.getId())
                .fetch()
                .rowsUpdated();
    }
}
