package com.example.notificationservice.repository;

import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.entity.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 알림 저장소 - 데이터 접근만 담당한다.
 * 모든 조회는 username 기반으로 사용자별 격리된다.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUsernameOrderByCreatedAtDesc(String username);

    List<Notification> findByUsernameOrderByIdDesc(String username, Pageable pageable);

    List<Notification> findByUsernameAndIdLessThanOrderByIdDesc(String username, Long cursor, Pageable pageable);

    List<Notification> findByUsernameAndIsReadFalse(String username);

    long countByUsernameAndIsReadFalse(String username);

    boolean existsByOrderIdAndType(Long orderId, NotificationType type);

    @Query("SELECT n FROM Notification n WHERE n.username = :username " +
           "AND (:type IS NULL OR n.type = :type) " +
           "AND (:cursor IS NULL OR n.id < :cursor) " +
           "ORDER BY n.id DESC")
    List<Notification> findByUsernameAndTypeWithCursor(
            @Param("username") String username,
            @Param("type") NotificationType type,
            @Param("cursor") Long cursor,
            Pageable pageable);
}
