package com.example.notificationservice.service;

import com.example.notificationservice.dto.CursorPage;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.entity.NotificationType;
import com.example.notificationservice.event.LowStockEvent;
import com.example.notificationservice.event.OrderCancelledEvent;
import com.example.notificationservice.event.OrderCreatedEvent;
import com.example.notificationservice.event.OrderStatusChangedEvent;
import com.example.notificationservice.exception.NotificationNotFoundException;
import com.example.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 알림 비즈니스 로직 담당 서비스
 * 모든 알림 조회/변경은 username 기반으로 사용자별 격리된다.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private static final Map<String, String> STATUS_LABELS = Map.of(
            "CREATED", "생성",
            "CONFIRMED", "확인",
            "SHIPPED", "배송중",
            "DELIVERED", "배송완료",
            "CANCELLED", "취소"
    );

    private static final Map<String, NotificationType> STATUS_TYPE_MAP = Map.of(
            "CONFIRMED", NotificationType.ORDER_CONFIRMED,
            "SHIPPED", NotificationType.ORDER_SHIPPED,
            "DELIVERED", NotificationType.ORDER_DELIVERED,
            "CANCELLED", NotificationType.ORDER_CANCELLED
    );

    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;

    public NotificationService(NotificationRepository notificationRepository,
                               SseEmitterService sseEmitterService) {
        this.notificationRepository = notificationRepository;
        this.sseEmitterService = sseEmitterService;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        String message = String.format("새 주문이 생성되었습니다: %s (수량: %d)",
                event.getProductName(), event.getQuantity());
        Notification notification = Notification.create(
                event.getUsername(), event.getOrderId(), NotificationType.ORDER_CREATED, message);
        Notification saved = notificationRepository.save(notification);
        sseEmitterService.sendToUser(event.getUsername(), saved);
    }

    @KafkaListener(
            topics = "order-status-events",
            groupId = "notification-group",
            containerFactory = "statusChangedKafkaListenerContainerFactory"
    )
    public void handleOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        String fromLabel = STATUS_LABELS.getOrDefault(event.getPreviousStatus(), event.getPreviousStatus());
        String toLabel = STATUS_LABELS.getOrDefault(event.getNewStatus(), event.getNewStatus());
        NotificationType type = STATUS_TYPE_MAP.getOrDefault(event.getNewStatus(), NotificationType.ORDER_CONFIRMED);
        String message = String.format("주문 상태가 변경되었습니다: %s (%s → %s)",
                event.getProductName(), fromLabel, toLabel);
        Notification notification = Notification.create(
                event.getUsername(), event.getOrderId(), type, message);
        Notification saved = notificationRepository.save(notification);
        sseEmitterService.sendToUser(event.getUsername(), saved);
    }

    @KafkaListener(
            topics = "order-cancelled-events",
            groupId = "notification-group",
            containerFactory = "cancelledKafkaListenerContainerFactory"
    )
    public void handleOrderCancelledEvent(OrderCancelledEvent event) {
        String message = String.format("주문이 취소되었습니다: %s (수량: %d, 재고 복원됨)",
                event.getProductName(), event.getQuantity());
        Notification notification = Notification.create(
                event.getUsername(), event.getOrderId(), NotificationType.ORDER_CANCELLED, message);
        Notification saved = notificationRepository.save(notification);
        sseEmitterService.sendToUser(event.getUsername(), saved);
    }

    @KafkaListener(
            topics = "low-stock-events",
            groupId = "notification-service",
            containerFactory = "lowStockKafkaListenerContainerFactory"
    )
    public void handleLowStockEvent(LowStockEvent event) {
        try {
            String message = String.format("상품 '%s'의 재고가 %d개 남았습니다.",
                    event.getProductName(), event.getRemainingStock());
            Notification notification = Notification.create(
                    "admin", null, NotificationType.LOW_STOCK, message);
            Notification saved = notificationRepository.save(notification);
            sseEmitterService.sendToUser("admin", saved);
        } catch (Exception e) {
            log.error("low-stock-events 처리 실패 productId={}", event.getProductId(), e);
        }
    }

    public List<Notification> getAllNotifications(String username) {
        return notificationRepository.findByUsernameOrderByCreatedAtDesc(username);
    }

    public Notification markAsRead(Long id, String username) {
        Notification notification = notificationRepository.findById(id)
                .filter(n -> n.getUsername().equals(username))
                .orElseThrow(() -> new NotificationNotFoundException(id));
        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    public void markAllAsRead(String username) {
        List<Notification> unread = notificationRepository.findByUsernameAndIsReadFalse(username);
        unread.forEach(Notification::markAsRead);
        notificationRepository.saveAll(unread);
    }

    public CursorPage<Notification> getNotificationsPaged(String username, NotificationType type, Long cursor, int size) {
        Pageable pageable = PageRequest.of(0, size + 1);
        List<Notification> notifications;
        if (type == null) {
            notifications = (cursor == null)
                    ? notificationRepository.findByUsernameOrderByIdDesc(username, pageable)
                    : notificationRepository.findByUsernameAndIdLessThanOrderByIdDesc(username, cursor, pageable);
        } else {
            notifications = notificationRepository.findByUsernameAndTypeWithCursor(username, type, cursor, pageable);
        }
        return CursorPage.of(notifications, size, Notification::getId);
    }

    public void deleteNotification(Long id, String username) {
        Notification notification = notificationRepository.findById(id)
                .filter(n -> n.getUsername().equals(username))
                .orElseThrow(() -> new NotificationNotFoundException(id));
        notificationRepository.delete(notification);
    }

    public void deleteAllNotifications(String username) {
        List<Notification> notifications = notificationRepository.findByUsernameOrderByCreatedAtDesc(username);
        notificationRepository.deleteAll(notifications);
    }

    public long countUnread(String username) {
        return notificationRepository.countByUsernameAndIsReadFalse(username);
    }
}
