package com.strk.orders.service;

import com.strk.common.model.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final ReactiveKafkaProducerTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${order-service.kafka.topic}")
    private String topic;

    public Mono<SenderResult<Void>> sendOrderEvent(OrderEvent orderEvent) {
        log.info("Sending order event to Kafka topic {}: {}", topic, orderEvent);

        return kafkaTemplate.send(topic, orderEvent.getOrderId().toString(), orderEvent)
                .doOnSuccess(senderResult ->
                        log.info("Sent order event {} to topic {} partition {} offset {}",
                                orderEvent.getOrderId(),
                                senderResult.recordMetadata().topic(),
                                senderResult.recordMetadata().partition(),
                                senderResult.recordMetadata().offset())
                )
                .doOnError(throwable ->
                        log.error("Error sending order event {} to topic {}: {}",
                                orderEvent.getOrderId(), topic, throwable.getMessage())
                );
    }
}