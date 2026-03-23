package com.example.orderservice.repository;

import com.example.orderservice.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * 최신 주문 순으로 페이지 단위 조회 (첫 페이지)
     */
    @Query("SELECT o FROM Order o ORDER BY o.id DESC")
    List<Order> findLatestOrders(Pageable pageable);

    /**
     * 커서 기반 페이지네이션 - 특정 커서 이전의 주문 조회
     */
    @Query("SELECT o FROM Order o WHERE o.id < :cursor ORDER BY o.id DESC")
    List<Order> findOrdersBefore(@Param("cursor") Long cursor, Pageable pageable);
}
