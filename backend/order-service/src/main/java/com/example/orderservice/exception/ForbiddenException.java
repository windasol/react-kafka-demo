package com.example.orderservice.exception;

/**
 * 리소스 접근 권한이 없을 때 발생하는 비즈니스 예외
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
