package com.example.notificationservice.repository;

import com.example.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
