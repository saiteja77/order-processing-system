package com.strk.notifications.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.strk.common.model.OrderEvent;
import com.strk.common.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private ReactiveKafkaConsumerTemplate<String, OrderEvent> kafkaConsumerTemplate;

    @InjectMocks
    private NotificationService notificationService;

    private final String TOPIC = "order-events";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "topic", TOPIC);
    }

    @Test
    void initializeConsumer_ReceivesAndStoresEvents() {
        // Given
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        OrderEvent event1 = createOrderEvent(orderId1, "Customer1", 100.0, OrderStatus.CREATED);
        OrderEvent event2 = createOrderEvent(orderId2, "Customer2", 200.0, OrderStatus.PROCESSING);

        ReceiverRecord<String, OrderEvent> record1 = mockReceiverRecord(0L, "key1", event1);
        ReceiverRecord<String, OrderEvent> record2 = mockReceiverRecord(1L, "key2", event2);

        when(kafkaConsumerTemplate.receive()).thenReturn(Flux.just(record1, record2));

        // When
        notificationService.initializeConsumer();

        // Add a small delay to allow the subscribe operation to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then
        verify(kafkaConsumerTemplate, times(1)).receive();
        verify(record1.receiverOffset(), times(1)).acknowledge();
        verify(record2.receiverOffset(), times(1)).acknowledge();

        // Verify the event history was populated
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, List<OrderEvent>> eventHistory =
                (ConcurrentHashMap<String, List<OrderEvent>>) ReflectionTestUtils.getField(notificationService, "eventHistory");

        assertNotNull(eventHistory);
        assertEquals(1, eventHistory.size());

        String key = TOPIC + "-0";
        assertTrue(eventHistory.containsKey(key));
        assertEquals(2, eventHistory.get(key).size());
        assertEquals(event1, eventHistory.get(key).get(0));
        assertEquals(event2, eventHistory.get(key).get(1));

        // Verify last offset was updated
        AtomicLong lastOffset = (AtomicLong) ReflectionTestUtils.getField(notificationService, "lastConsumedOffset");
        assertNotNull(lastOffset);
        assertEquals(1L, lastOffset.get());
    }

    @Test
    void getNotifications_ReturnsStoredEvents() {
        // Given
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        OrderEvent event1 = createOrderEvent(orderId1, "Customer1", 100.0, OrderStatus.CREATED);
        OrderEvent event2 = createOrderEvent(orderId2, "Customer2", 200.0, OrderStatus.PROCESSING);

        // Manually populate the event history
        ConcurrentHashMap<String, List<OrderEvent>> eventHistory = new ConcurrentHashMap<>();
        List<OrderEvent> events = new ArrayList<>();
        events.add(event1);
        events.add(event2);
        eventHistory.put(TOPIC + "-0", events);

        ReflectionTestUtils.setField(notificationService, "eventHistory", eventHistory);

        // When
        Flux<OrderEvent> result = notificationService.getNotifications();

        // Then
        StepVerifier.create(result)
                .expectNext(event1)
                .expectNext(event2)
                .verifyComplete();
    }

    @Test
    void getNotifications_ReturnsEmptyFlux_WhenNoEvents() {
        // Given
        ConcurrentHashMap<String, List<OrderEvent>> eventHistory = new ConcurrentHashMap<>();
        ReflectionTestUtils.setField(notificationService, "eventHistory", eventHistory);

        // When
        Flux<OrderEvent> result = notificationService.getNotifications();

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void getNotifications_HandlesMultiplePartitions() {
        // Given
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        UUID orderId3 = UUID.randomUUID();

        OrderEvent event1 = createOrderEvent(orderId1, "Customer1", 100.0, OrderStatus.CREATED);
        OrderEvent event2 = createOrderEvent(orderId2, "Customer2", 200.0, OrderStatus.PROCESSING);
        OrderEvent event3 = createOrderEvent(orderId3, "Customer3", 300.0, OrderStatus.SHIPPED);

        // Manually populate the event history with multiple partitions
        ConcurrentHashMap<String, List<OrderEvent>> eventHistory = new ConcurrentHashMap<>();

        List<OrderEvent> events1 = new ArrayList<>();
        events1.add(event1);
        eventHistory.put(TOPIC + "-0", events1);

        List<OrderEvent> events2 = new ArrayList<>();
        events2.add(event2);
        events2.add(event3);
        eventHistory.put(TOPIC + "-1", events2);

        ReflectionTestUtils.setField(notificationService, "eventHistory", eventHistory);

        // When
        Flux<OrderEvent> result = notificationService.getNotifications();

        // Then
        StepVerifier.create(result)
                .expectNextCount(3)
                .verifyComplete();
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

    @SuppressWarnings("unchecked")
    private ReceiverRecord<String, OrderEvent> mockReceiverRecord(long offset, String key, OrderEvent value) {
        ReceiverRecord<String, OrderEvent> record = mock(ReceiverRecord.class);
        ReceiverOffset receiverOffset = mock(ReceiverOffset.class);

        when(record.topic()).thenReturn("order-events");
        when(record.partition()).thenReturn(0);
        when(record.value()).thenReturn(value);
        when(record.receiverOffset()).thenReturn(receiverOffset);
        when(receiverOffset.offset()).thenReturn(offset);

        return record;
    }
}