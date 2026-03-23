package com.example.orderservice.exception;

/**
 * 주문을 찾을 수 없을 때 발생하는 비즈니스 예외
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("주문을 찾을 수 없습니다. id=" + id);
    }
}
