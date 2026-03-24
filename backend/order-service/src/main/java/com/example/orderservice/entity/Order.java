package com.example.orderservice.entity;

import com.example.orderservice.exception.InvalidOrderStatusException;
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

    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // JPA 기본 생성자 (외부 직접 사용 금지)
    protected Order() {}

    /**
     * 주문 생성 팩토리 메서드
     */
    public static Order create(Long productId, String productName, Integer quantity) {
        Order order = new Order();
        order.productId = productId;
        order.productName = productName;
        order.quantity = quantity;
        return order;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.CREATED;
    }

    /**
     * 주문 상태를 다음 단계로 전이하는 도메인 메서드
     * 유효하지 않은 전이 시 InvalidOrderStatusException 발생
     */
    public void changeStatus(OrderStatus targetStatus) {
        if (!this.status.canTransitionTo(targetStatus)) {
            throw new InvalidOrderStatusException(this.status, targetStatus);
        }
        this.status = targetStatus;
    }

    /**
     * 주문 확인 (CREATED → CONFIRMED)
     */
    public void confirm() {
        changeStatus(OrderStatus.CONFIRMED);
    }

    /**
     * 주문 배송 시작 (CONFIRMED → SHIPPED)
     */
    public void ship() {
        changeStatus(OrderStatus.SHIPPED);
    }

    /**
     * 주문 배송 완료 (SHIPPED → DELIVERED)
     */
    public void deliver() {
        changeStatus(OrderStatus.DELIVERED);
    }
}
