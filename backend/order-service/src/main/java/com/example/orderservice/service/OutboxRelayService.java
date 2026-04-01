package com.example.orderservice.service;

import com.example.orderservice.entity.OutboxEvent;
import com.example.orderservice.event.LowStockEvent;
import com.example.orderservice.event.OrderCancelledEvent;
import com.example.orderservice.event.OrderCreatedEvent;
import com.example.orderservice.event.OrderStatusChangedEvent;
import com.example.orderservice.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboxRelayService {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayService.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxRelayService(OutboxRepository outboxRepository,
                              KafkaTemplate<String, Object> kafkaTemplate,
                              ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 1000)
    public void relay() {
        List<OutboxEvent> events = outboxRepository.findTop100ByOrderByCreatedAtAsc();
        for (OutboxEvent event : events) {
            try {
                Object payload = deserialize(event);
                kafkaTemplate.send(event.getTopic(), event.getMessageKey(), payload);
                outboxRepository.delete(event);
            } catch (Exception e) {
                log.error("Outbox 릴레이 실패 id={} topic={} eventType={}",
                        event.getId(), event.getTopic(), event.getEventType(), e);
            }
        }
    }

    private Object deserialize(OutboxEvent event) throws Exception {
        return switch (event.getEventType()) {
            case "OrderCreatedEvent" -> objectMapper.readValue(event.getPayload(), OrderCreatedEvent.class);
            case "OrderStatusChangedEvent" -> objectMapper.readValue(event.getPayload(), OrderStatusChangedEvent.class);
            case "OrderCancelledEvent" -> objectMapper.readValue(event.getPayload(), OrderCancelledEvent.class);
            case "LowStockEvent" -> objectMapper.readValue(event.getPayload(), LowStockEvent.class);
            default -> throw new IllegalStateException("알 수 없는 이벤트 타입: " + event.getEventType());
        };
    }
}
