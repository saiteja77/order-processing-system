package com.strk.orders.utils;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        try {
            // Perform your custom health check
            boolean isHealthy = performHealthCheck();

            if (isHealthy) {
                return Health.up().build();
            } else {
                return Health.down()
                        .withDetail("Error", "Custom health check failed")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("Error", e.getMessage())
                    .build();
        }
    }

    private boolean performHealthCheck() {
        // Implement your specific health check logic
        // Could check database connection, external service, etc.
        return true;
    }
}