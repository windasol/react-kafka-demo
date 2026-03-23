package com.example.orderservice.entity;

import java.util.Map;
import java.util.Set;

/**
 * 주문 상태 열거형
 * CREATED → CONFIRMED → SHIPPED → DELIVERED 순서로 전이된다.
 */
public enum OrderStatus {

    CREATED,
    CONFIRMED,
    SHIPPED,
    DELIVERED;

    // 각 상태에서 전이 가능한 다음 상태 목록
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            CREATED, Set.of(CONFIRMED),
            CONFIRMED, Set.of(SHIPPED),
            SHIPPED, Set.of(DELIVERED),
            DELIVERED, Set.of()
    );

    /**
     * 현재 상태에서 대상 상태로 전이 가능한지 확인
     */
    public boolean canTransitionTo(OrderStatus target) {
        return VALID_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
