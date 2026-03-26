package com.example.orderservice.repository;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 저장소 - 데이터 접근만 담당한다.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 최신 주문 순으로 전체 주문 목록 조회
     */
    List<Order> findAllByOrderByCreatedAtDesc();

    /**
     * 최신 주문 순으로 페이지 단위 조회 (첫 페이지)
     */
    List<Order> findAllByOrderByIdDesc(Pageable pageable);

    /**
     * 커서 기반 페이지네이션 - 특정 커서 이전의 주문 조회
     */
    List<Order> findByIdLessThanOrderByIdDesc(Long cursor, Pageable pageable);

    /**
     * 상품명, 상태, 날짜 범위로 필터링 (동적 조건)
     */
    @Query("SELECT o FROM Order o WHERE " +
           "(:keyword IS NULL OR o.productName LIKE %:keyword%) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:from IS NULL OR o.createdAt >= :from) AND " +
           "(:to IS NULL OR o.createdAt <= :to) " +
           "ORDER BY o.id DESC")
    List<Order> findOrdersByFilter(
            @Param("keyword") String keyword,
            @Param("status") OrderStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    /**
     * 필터링 + 커서 기반 페이지네이션
     */
    @Query("SELECT o FROM Order o WHERE " +
           "o.id < :cursor AND " +
           "(:keyword IS NULL OR o.productName LIKE %:keyword%) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:from IS NULL OR o.createdAt >= :from) AND " +
           "(:to IS NULL OR o.createdAt <= :to) " +
           "ORDER BY o.id DESC")
    List<Order> findOrdersByFilterBefore(
            @Param("cursor") Long cursor,
            @Param("keyword") String keyword,
            @Param("status") OrderStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    /**
     * 오프셋 기반 필터 검색 (Page 반환)
     */
    @Query("SELECT o FROM Order o WHERE " +
           "(:keyword IS NULL OR o.productName LIKE %:keyword%) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:from IS NULL OR o.createdAt >= :from) AND " +
           "(:to IS NULL OR o.createdAt <= :to)")
    Page<Order> searchByFilter(
            @Param("keyword") String keyword,
            @Param("status") OrderStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);
}
