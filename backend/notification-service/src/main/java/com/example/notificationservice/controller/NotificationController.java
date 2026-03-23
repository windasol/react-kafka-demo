package com.example.notificationservice.controller;

import com.example.notificationservice.dto.CursorPage;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.service.NotificationService;
import com.example.notificationservice.service.SseEmitterService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    /**
     * 알림 목록 커서 기반 페이지네이션 API
     */
    @GetMapping(params = "paged")
    public ResponseEntity<CursorPage<Notification>> getNotificationsPaged(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(notificationService.getNotificationsPaged(cursor, size));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications() {
        return sseEmitterService.createEmitter();
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread()));
    }
}
