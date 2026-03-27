package com.example.notificationservice.service;

import com.example.notificationservice.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE 연결 관리 서비스 — username 별로 emitter를 관리한다.
 * 알림은 해당 사용자의 연결에만 전송된다.
 */
@Service
public class SseEmitterService {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterService.class);

    // username → 해당 사용자의 SSE 연결 목록
    private final ConcurrentHashMap<String, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    /**
     * 특정 사용자의 SSE 연결 생성 (타임아웃 30분)
     */
    public SseEmitter createEmitter(String username) {
        SseEmitter emitter = new SseEmitter(1_800_000L);

        userEmitters.computeIfAbsent(username, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(username, emitter));
        emitter.onTimeout(() -> {
            removeEmitter(username, emitter);
            emitter.complete();
        });
        emitter.onError(e -> removeEmitter(username, emitter));

        try {
            emitter.send(SseEmitter.event().name("ping").data("connected"));
        } catch (IOException e) {
            removeEmitter(username, emitter);
        }

        return emitter;
    }

    /**
     * 특정 사용자에게만 알림 전송
     */
    public void sendToUser(String username, Notification notification) {
        List<SseEmitter> emitters = userEmitters.getOrDefault(username, List.of());
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(notification));
            } catch (IOException e) {
                log.warn("SSE 전송 실패 [{}], emitter 제거: {}", username, e.getMessage());
                removeEmitter(username, emitter);
            }
        }
    }

    private void removeEmitter(String username, SseEmitter emitter) {
        List<SseEmitter> emitters = userEmitters.get(username);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(username);
            }
        }
    }
}
