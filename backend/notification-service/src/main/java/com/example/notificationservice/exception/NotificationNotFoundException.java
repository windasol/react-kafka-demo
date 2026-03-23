package com.example.notificationservice.exception;

/**
 * 알림을 찾을 수 없을 때 발생하는 비즈니스 예외
 */
public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(Long id) {
        super("알림을 찾을 수 없습니다. id=" + id);
    }
}
