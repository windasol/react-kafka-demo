package com.example.notificationservice.service;

import com.example.notificationservice.dto.CursorPage;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.event.OrderCancelledEvent;
import com.example.notificationservice.event.OrderCreatedEvent;
import com.example.notificationservice.event.OrderStatusChangedEvent;
import com.example.notificationservice.exception.NotificationNotFoundException;
import com.example.notificationservice.repository.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 알림 비즈니스 로직 담당 서비스
 * HTTP 관련 코드는 포함하지 않는다. (SRP 원칙)
 */
@Service
public class NotificationService {

    // 상태별 한국어 표시명
    private static final Map<String, String> STATUS_LABELS = Map.of(
            "CREATED", "생성",
            "CONFIRMED", "확인",
            "SHIPPED", "배송중",
            "DELIVERED", "배송완료"
    );

    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;

    public NotificationService(NotificationRepository notificationRepository,
                               SseEmitterService sseEmitterService) {
        this.notificationRepository = notificationRepository;
        this.sseEmitterService = sseEmitterService;
    }

    /**
     * Kafka 주문 생성 이벤트 수신 후 알림 저장 및 SSE 브로드캐스트
     */
    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        // 도메인 팩토리 메서드로 생성 (Setter 직접 사용 금지)
        String message = String.format("새 주문이 생성되었습니다: %s (수량: %d)",
                event.getProductName(), event.getQuantity());
        Notification notification = Notification.create(event.getOrderId(), message);

        Notification saved = notificationRepository.save(notification);

        // 연결된 모든 SSE 클라이언트에 실시간 알림 전송
        sseEmitterService.broadcast(saved);
    }

    /**
     * Kafka 주문 상태 변경 이벤트 수신 후 알림 저장 및 SSE 브로드캐스트
     */
    @KafkaListener(
            topics = "order-status-events",
            groupId = "notification-group",
            containerFactory = "statusChangedKafkaListenerContainerFactory"
    )
    public void handleOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        String fromLabel = STATUS_LABELS.getOrDefault(event.getPreviousStatus(), event.getPreviousStatus());
        String toLabel = STATUS_LABELS.getOrDefault(event.getNewStatus(), event.getNewStatus());

        String message = String.format("주문 상태가 변경되었습니다: %s (%s → %s)",
                event.getProductName(), fromLabel, toLabel);
        Notification notification = Notification.create(event.getOrderId(), message);

        Notification saved = notificationRepository.save(notification);

        sseEmitterService.broadcast(saved);
    }

    /**
     * Kafka 주문 취소 이벤트 수신 후 알림 저장 및 SSE 브로드캐스트
     */
    @KafkaListener(
            topics = "order-cancelled-events",
            groupId = "notification-group",
            containerFactory = "cancelledKafkaListenerContainerFactory"
    )
    public void handleOrderCancelledEvent(OrderCancelledEvent event) {
        String message = String.format("주문이 취소되었습니다: %s (수량: %d, 재고 복원됨)",
                event.getProductName(), event.getQuantity());
        Notification notification = Notification.create(event.getOrderId(), message);

        Notification saved = notificationRepository.save(notification);
        sseEmitterService.broadcast(saved);
    }

    /**
     * 최신 순으로 전체 알림 목록 조회
     */
    public List<Notification> getAllNotifications() {
        return notificationRepository.findLatestNotifications();
    }

    /**
     * 특정 알림 읽음 처리
     * 알림 미존재 시 NotificationNotFoundException 발생 (커스텀 예외 사용)
     */
    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));

        // 도메인 메서드로 상태 변경 (Setter 직접 사용 금지)
        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    /**
     * 읽지 않은 알림 전체 읽음 처리
     */
    public void markAllAsRead() {
        List<Notification> unread = notificationRepository.findUnreadNotifications();
        // 도메인 메서드로 상태 변경
        unread.forEach(Notification::markAsRead);
        notificationRepository.saveAll(unread);
    }

    /**
     * 커서 기반 페이지네이션으로 알림 목록 조회
     * hasNext 판단을 위해 size + 1개를 조회한다.
     */
    public CursorPage<Notification> getNotificationsPaged(Long cursor, int size) {
        Pageable pageable = PageRequest.of(0, size + 1);

        List<Notification> notifications = (cursor == null)
                ? notificationRepository.findLatestNotifications(pageable)
                : notificationRepository.findNotificationsBefore(cursor, pageable);

        return CursorPage.of(notifications, size, Notification::getId);
    }

    /**
     * 개별 알림 삭제
     * 알림 미존재 시 NotificationNotFoundException 발생
     */
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new NotificationNotFoundException(id);
        }
        notificationRepository.deleteById(id);
    }

    /**
     * 전체 알림 삭제
     */
    public void deleteAllNotifications() {
        notificationRepository.deleteAll();
    }

    /**
     * 읽지 않은 알림 수 조회
     */
    public long countUnread() {
        return notificationRepository.countUnreadNotifications();
    }
}
