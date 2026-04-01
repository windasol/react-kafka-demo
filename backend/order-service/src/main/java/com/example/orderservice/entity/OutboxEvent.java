package com.example.orderservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topic;

    @Column(name = "message_key", nullable = false)
    private String messageKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected OutboxEvent() {}

    public static OutboxEvent of(String topic, String messageKey, String payload, String eventType) {
        OutboxEvent event = new OutboxEvent();
        event.topic = topic;
        event.messageKey = messageKey;
        event.payload = payload;
        event.eventType = eventType;
        event.createdAt = LocalDateTime.now();
        return event;
    }

    public Long getId() { return id; }
    public String getTopic() { return topic; }
    public String getMessageKey() { return messageKey; }
    public String getPayload() { return payload; }
    public String getEventType() { return eventType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
