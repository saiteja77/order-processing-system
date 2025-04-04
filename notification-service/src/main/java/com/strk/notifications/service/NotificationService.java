package com.strk.notifications.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.strk.common.model.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverOffset;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final ReactiveKafkaConsumerTemplate<String, OrderEvent> kafkaConsumerTemplate;

    @Value("${notification-service.kafka.topic}")
    private String topic;

    private final Map<String, List<OrderEvent>> eventHistory = new ConcurrentHashMap<>();
    private final AtomicLong lastConsumedOffset = new AtomicLong(-1);

    /**
     * @author saiteja77
     * Initializes Kafka Comnsumer whose functionality is to log the event whevever an order is created or updated
     * in order-service and store the history of events
     */
    @PostConstruct
    public void initializeConsumer() {
        log.info("Initializing order event history consumer for topic: {}", topic);

        // Create a unique consumer group for this history service to get all messages
        kafkaConsumerTemplate.receive()
                .doOnNext(record -> {
                    ReceiverOffset offset = record.receiverOffset();
                    OrderEvent event = record.value();
                    String key = String.format("%s-%d", record.topic(), record.partition());

                    // Store the event in our history
                    eventHistory.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()))
                            .add(event);

                    log.info("Stored event in history: topic={}, partition={}, offset={}, Order={}",
                            record.topic(), record.partition(), offset.offset(), event.toString());

                    // Acknowledge the record
                    offset.acknowledge();
                    lastConsumedOffset.set(offset.offset());
                })
                .doOnError(error -> log.error("Error consuming events for history: {}", error.getMessage()))
                .subscribe();
    }

    /**
     * @author saiteja77
     * Returns the OrderEvents in Kafka topic history
     */
    public Flux<OrderEvent> getNotifications() {
        return Flux.fromIterable(eventHistory.entrySet())
                .flatMap(entry -> Flux.fromIterable(entry.getValue()))
                .doOnSubscribe(sub -> log.info("Returning {} events from history",
                        eventHistory.values().stream().mapToInt(List::size).sum()));
    }
}