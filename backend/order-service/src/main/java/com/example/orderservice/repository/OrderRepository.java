package com.example.orderservice.repository;

import com.example.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 주문 저장소 - 데이터 접근만 담당한다.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 최신 주문 순으로 전체 주문 목록 조회
     */
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findLatestOrders();
}
