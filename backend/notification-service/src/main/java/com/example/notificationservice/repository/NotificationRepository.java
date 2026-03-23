package com.example.notificationservice.repository;

import com.example.notificationservice.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 알림 저장소 - 데이터 접근만 담당한다.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 최신 알림 순으로 전체 알림 목록 조회
     */
    @Query("SELECT n FROM Notification n ORDER BY n.createdAt DESC")
    List<Notification> findLatestNotifications();

    /**
     * 최신 알림 순으로 페이지 단위 조회 (첫 페이지)
     */
    @Query("SELECT n FROM Notification n ORDER BY n.id DESC")
    List<Notification> findLatestNotifications(Pageable pageable);

    /**
     * 커서 기반 페이지네이션 - 특정 커서 이전의 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.id < :cursor ORDER BY n.id DESC")
    List<Notification> findNotificationsBefore(@Param("cursor") Long cursor, Pageable pageable);

    /**
     * 읽지 않은 알림 목록 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.isRead = false")
    List<Notification> findUnreadNotifications();

    /**
     * 읽지 않은 알림 수 조회
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isRead = false")
    long countUnreadNotifications();
}
