package com.strk.notifications.controller;

import com.strk.common.model.OrderEvent;
import com.strk.notifications.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * @author saiteja77
     * Returns the OrderEvents in Kafka topic history
     * @return Flux<OrderEvent>
     */
    @GetMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Flux<OrderEvent> getNotifications() {
        return notificationService.getNotifications();
    }
}