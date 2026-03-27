package com.example.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 생성 도메인 이벤트 (Kafka 발행용)
 * 이벤트 객체는 불변이어야 하므로 Setter를 제공하지 않는다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private Long orderId;
    private String username;
    private String productName;
    private Integer quantity;
    private String status;
}
