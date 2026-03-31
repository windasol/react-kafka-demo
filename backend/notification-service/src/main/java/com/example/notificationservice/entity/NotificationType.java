package com.example.notificationservice.entity;

/**
 * 알림 타입 열거형
 * 주문 이벤트 종류에 따라 알림을 분류한다.
 */
public enum NotificationType {

    ORDER_CREATED,
    ORDER_CONFIRMED,
    ORDER_SHIPPED,
    ORDER_DELIVERED,
    ORDER_CANCELLED,
    LOW_STOCK
}
