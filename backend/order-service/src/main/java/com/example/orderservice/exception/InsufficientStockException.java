package com.example.orderservice.exception;

/**
 * 재고 부족 예외
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productName, int requested, int available) {
        super(String.format("재고가 부족합니다. (상품: %s, 요청: %d, 남은 재고: %d)",
                productName, requested, available));
    }
}
