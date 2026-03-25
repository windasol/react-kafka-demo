package com.example.notificationservice.repository;

import com.example.notificationservice.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 알림 저장소 - 데이터 접근만 담당한다.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 최신 알림 순으로 전체 알림 목록 조회
     */
    List<Notification> findAllByOrderByCreatedAtDesc();

    /**
     * 최신 알림 순으로 페이지 단위 조회 (첫 페이지)
     */
    List<Notification> findAllByOrderByIdDesc(Pageable pageable);

    /**
     * 커서 기반 페이지네이션 - 특정 커서 이전의 알림 조회
     */
    List<Notification> findByIdLessThanOrderByIdDesc(Long cursor, Pageable pageable);

    /**
     * 읽지 않은 알림 목록 조회
     */
    List<Notification> findByIsReadFalse();

    /**
     * 읽지 않은 알림 수 조회
     */
    long countByIsReadFalse();
}
