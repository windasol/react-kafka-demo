package com.example.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 취소 도메인 이벤트 (Kafka 발행용)
 * 재고 복원을 위한 정보를 포함한다. (보상 트랜잭션)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {

    private Long orderId;
    private String username;
    private Long productId;
    private String productName;
    private Integer quantity;
}
