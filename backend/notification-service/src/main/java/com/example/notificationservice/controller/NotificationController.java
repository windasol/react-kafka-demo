package com.example.notificationservice.controller;

import com.example.notificationservice.dto.CursorPage;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.entity.NotificationType;
import com.example.notificationservice.service.NotificationService;
import com.example.notificationservice.service.SseEmitterService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * 알림 API Presentation Layer
 * 모든 요청은 인증된 사용자 기준으로 격리된다.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;

    public NotificationController(NotificationService notificationService,
                                  SseEmitterService sseEmitterService) {
        this.notificationService = notificationService;
        this.sseEmitterService = sseEmitterService;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getAllNotifications(authentication.getName()));
    }

    @GetMapping(params = "paged")
    public ResponseEntity<CursorPage<Notification>> getNotificationsPaged(
            Authentication authentication,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "7") int size) {
        return ResponseEntity.ok(notificationService.getNotificationsPaged(authentication.getName(), type, cursor, size));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(Authentication authentication) {
        return sseEmitterService.createEmitter(authentication.getName());
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(notificationService.markAsRead(id, authentication.getName()));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id, Authentication authentication) {
        notificationService.deleteNotification(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotifications(Authentication authentication) {
        notificationService.deleteAllNotifications(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread(authentication.getName())));
    }
}
