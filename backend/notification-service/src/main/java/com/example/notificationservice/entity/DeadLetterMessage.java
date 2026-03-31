package com.example.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dead_letter_messages")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeadLetterMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime failedAt;

    private boolean retried;
    private LocalDateTime retriedAt;

    public void markRetried() {
        this.retried = true;
        this.retriedAt = LocalDateTime.now();
    }
}
