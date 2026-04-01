package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderStatsSummary;
import com.example.orderservice.dto.PageResponse;
import com.example.orderservice.dto.OrderStatusRequest;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
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
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest request,
                                             Authentication authentication) {
        Order created = orderService.placeOrder(request, authentication.getName());
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
     * 주문 목록 오프셋 기반 페이지네이션 API
     */
    @GetMapping(params = "paged")
    public ResponseEntity<PageResponse<Order>> getOrdersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size) {
        return ResponseEntity.ok(orderService.getOrdersPaged(page, size));
    }

    /**
     * 주문 검색/필터 API (상품명, 상태, 날짜 범위)
     */
    @GetMapping(params = "search")
    public ResponseEntity<PageResponse<Order>> searchOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {
        return ResponseEntity.ok(
                orderService.searchOrders(page, size, keyword, status, dateFrom, dateTo));
    }

    /**
     * 주문 상세 조회 API
     */
    @PreAuthorize("@orderSecurity.isOwner(#id, authentication.name)")
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(orderService.getOrder(id, authentication.getName()));
    }

    /**
     * 주문 상태 변경 API
     * CREATED → CONFIRMED → SHIPPED → DELIVERED 순서로만 전이 가능
     */
    @PreAuthorize("@orderSecurity.isOwner(#id, authentication.name)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> changeOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusRequest request,
            Authentication authentication) {
        Order updated = orderService.changeOrderStatus(id, request.status(), authentication.getName());
        return ResponseEntity.ok(updated);
    }

    /**
     * 주문 취소 API
     * CREATED, CONFIRMED 상태에서만 취소 가능. 재고가 복원된다.
     */
    @PreAuthorize("@orderSecurity.isOwner(#id, authentication.name)")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id, Authentication authentication) {
        Order cancelled = orderService.cancelOrder(id, authentication.getName());
        return ResponseEntity.ok(cancelled);
    }

    /**
     * 주문 통계 API
     * 사용자별 주문 집계 및 최근 7일 일별 통계를 반환한다.
     */
    @GetMapping("/stats")
    public ResponseEntity<OrderStatsSummary> getOrderStats(Authentication authentication) {
        OrderStatsSummary stats = orderService.getStatsSummary(authentication.getName());
        return ResponseEntity.ok(stats);
    }

    /**
     * 주문 CSV 내보내기 API
     * 사용자의 전체 주문 목록을 CSV 파일로 반환한다.
     */
    @GetMapping("/export")
    public ResponseEntity<Resource> exportOrders(Authentication authentication) {
        String csv = orderService.exportOrdersCsv(authentication.getName());
        byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(csvBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("orders.csv").build());

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .contentLength(csvBytes.length)
                .body(resource);
    }
}
