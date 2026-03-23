package com.example.orderservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 주문 도메인 엔티티
 * 상태 변경은 반드시 도메인 메서드를 통해 수행한다. (Setter 직접 사용 금지)
 */
@Getter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // JPA 기본 생성자 (외부 직접 사용 금지)
    protected Order() {}

    /**
     * 주문 생성 팩토리 메서드
     */
    public static Order create(String productName, Integer quantity) {
        Order order = new Order();
        order.productName = productName;
        order.quantity = quantity;
        return order;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = "CREATED";
    }
}
