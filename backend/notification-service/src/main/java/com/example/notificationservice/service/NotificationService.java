package com.example.notificationservice.service;

import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.event.OrderCreatedEvent;
import com.example.notificationservice.repository.NotificationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;

    public NotificationService(NotificationRepository notificationRepository,
                               SseEmitterService sseEmitterService) {
        this.notificationRepository = notificationRepository;
        this.sseEmitterService = sseEmitterService;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        Notification notification = new Notification();
        notification.setMessage(
                String.format("새 주문이 생성되었습니다: %s (수량: %d)",
                        event.getProductName(), event.getQuantity())
        );
        notification.setOrderId(event.getOrderId());
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);
        sseEmitterService.broadcast(saved);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다: " + id));
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    public void markAllAsRead() {
        List<Notification> unread = notificationRepository.findByIsReadFalse();
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    public long countUnread() {
        return notificationRepository.countByIsReadFalse();
    }
}
