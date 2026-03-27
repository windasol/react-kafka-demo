package com.example.notificationservice.repository;

import com.example.notificationservice.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
