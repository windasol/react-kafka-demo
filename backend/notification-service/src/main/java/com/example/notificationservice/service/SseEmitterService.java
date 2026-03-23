package com.example.notificationservice.service;

import com.example.notificationservice.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE(Server-Sent Events) 연결 관리 서비스
 * 클라이언트 연결 생성/제거 및 실시간 알림 브로드캐스트를 담당한다.
 */
@Service
public class SseEmitterService {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterService.class);

    // 동시성 안전한 리스트로 활성 연결 관리
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * 새 SSE 연결 생성 (타임아웃 30분)
     * 연결 즉시 ping 이벤트를 전송해 응답 헤더를 flush한다.
     */
    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(1_800_000L);

        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            emitter.complete();
        });
        emitter.onError(e -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("ping").data("connected"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    /**
     * 연결된 모든 클라이언트에 알림 브로드캐스트
     * 전송 실패한 emitter는 즉시 제거한다.
     */
    public void broadcast(Notification notification) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
            } catch (IOException e) {
                log.warn("SSE 전송 실패, emitter 제거: {}", e.getMessage());
                emitters.remove(emitter);
            }
        }
    }
}
