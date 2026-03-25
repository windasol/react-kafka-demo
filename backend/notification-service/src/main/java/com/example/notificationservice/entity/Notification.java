package com.example.notificationservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 알림 도메인 엔티티
 * 상태 변경은 반드시 도메인 메서드를 통해 수행한다. (Setter 직접 사용 금지)
 */
@Getter
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isRead = false;

    // JPA 기본 생성자 (외부 직접 사용 금지)
    protected Notification() {}

    /**
     * 알림 생성 팩토리 메서드
     */
    public static Notification create(Long orderId, NotificationType type, String message) {
        Notification notification = new Notification();
        notification.orderId = orderId;
        notification.type = type;
        notification.message = message;
        notification.isRead = false;
        return notification;
    }

    /**
     * 알림 읽음 처리 도메인 메서드
     * notification.setIsRead(true) 대신 이 메서드를 사용한다.
     */
    public void markAsRead() {
        this.isRead = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
