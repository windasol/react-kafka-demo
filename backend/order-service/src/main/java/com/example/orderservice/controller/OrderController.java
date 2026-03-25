package com.example.orderservice.controller;

import com.example.orderservice.dto.CursorPage;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderStatusRequest;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 주문 API Presentation Layer
 * Service만 호출한다. Repository를 직접 참조하지 않는다. (레이어드 아키텍처 원칙)
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 주문 생성 API
     * @Valid로 요청 입력값을 검증한다. (방어적 프로그래밍)
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest request) {
        Order created = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 주문 목록 조회 API
     */
    @GetMapping
    public ResponseEntity<List<Order>> getOrders() {
        return ResponseEntity.ok(orderService.getOrders());
    }

    /**
     * 주문 목록 커서 기반 페이지네이션 API
     */
    @GetMapping(params = "paged")
    public ResponseEntity<CursorPage<Order>> getOrdersPaged(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getOrdersPaged(cursor, size));
    }

    /**
     * 주문 검색/필터 API (상품명, 상태, 날짜 범위)
     */
    @GetMapping(params = "search")
    public ResponseEntity<CursorPage<Order>> searchOrders(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {
        return ResponseEntity.ok(
                orderService.searchOrders(cursor, size, keyword, status, dateFrom, dateTo));
    }

    /**
     * 주문 상세 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    /**
     * 주문 상태 변경 API
     * CREATED → CONFIRMED → SHIPPED → DELIVERED 순서로만 전이 가능
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> changeOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusRequest request) {
        Order updated = orderService.changeOrderStatus(id, request.status());
        return ResponseEntity.ok(updated);
    }

    /**
     * 주문 취소 API
     * CREATED, CONFIRMED 상태에서만 취소 가능. 재고가 복원된다.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        Order cancelled = orderService.cancelOrder(id);
        return ResponseEntity.ok(cancelled);
    }
}
