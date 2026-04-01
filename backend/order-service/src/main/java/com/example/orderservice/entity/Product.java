package com.example.orderservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 상품 도메인 엔티티
 * 재고 변경은 반드시 도메인 메서드를 통해 수행한다. (Setter 직접 사용 금지)
 */
@Getter
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // JPA 기본 생성자 (외부 직접 사용 금지)
    protected Product() {}

    /**
     * 상품 생성 팩토리 메서드
     */
    public static Product create(String name, Integer price, Integer stock) {
        Product product = new Product();
        product.name = name;
        product.price = price;
        product.stock = stock;
        return product;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 상품 정보 수정 도메인 메서드
     */
    public void update(String name, Integer price, Integer stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    /**
     * 재고 차감 도메인 메서드
     * 재고 부족 시 false 반환
     */
    public boolean deductStock(int quantity) {
        if (this.stock < quantity) {
            return false;
        }
        this.stock -= quantity;
        return true;
    }

    /**
     * 재고 복원 도메인 메서드 (주문 취소 시 사용)
     */
    public void restoreStock(int quantity) {
        this.stock += quantity;
    }
}
