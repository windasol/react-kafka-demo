package com.example.orderservice.exception;

import com.example.orderservice.entity.OrderStatus;

/**
 * 유효하지 않은 주문 상태 전이 시 발생하는 비즈니스 예외
 */
public class InvalidOrderStatusException extends RuntimeException {

    public InvalidOrderStatusException(OrderStatus currentStatus, OrderStatus targetStatus) {
        super(String.format("주문 상태를 %s에서 %s로 변경할 수 없습니다.", currentStatus, targetStatus));
    }
}
