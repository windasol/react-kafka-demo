package com.example.orderservice.service;

import com.example.orderservice.dto.CursorPage;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.entity.Product;
import com.example.orderservice.event.OrderCancelledEvent;
import com.example.orderservice.event.OrderCreatedEvent;
import com.example.orderservice.event.OrderStatusChangedEvent;
import com.example.orderservice.exception.InsufficientStockException;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.exception.ProductNotFoundException;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 주문 비즈니스 로직 담당 서비스
 * HTTP 관련 코드는 포함하지 않는다. (SRP 원칙)
 */
@Service
public class OrderService {

    private static final String ORDER_EVENTS_TOPIC = "order-events";
    private static final String ORDER_STATUS_TOPIC = "order-status-events";
    private static final String ORDER_CANCELLED_TOPIC = "order-cancelled-events";

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 주문 생성 후 Kafka 이벤트 발행
     * 상품 재고를 차감한다. 재고 부족 시 InsufficientStockException 발생
     */
    @Transactional
    public Order placeOrder(OrderRequest request) {
        Objects.requireNonNull(request, "주문 요청 정보는 필수입니다.");

        // 상품 조회 및 재고 차감
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        if (!product.deductStock(request.quantity())) {
            throw new InsufficientStockException(
                    product.getName(), request.quantity(), product.getStock());
        }
        productRepository.save(product);

        // 주문 생성 (주문 시점의 단가를 함께 저장)
        Order order = Order.create(product.getId(), product.getName(), request.quantity(), product.getPrice());
        Order savedOrder = orderRepository.save(order);

        // 주문 생성 이벤트 발행
        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getProductName(),
                savedOrder.getQuantity(),
                savedOrder.getStatus().name()
        );
        kafkaTemplate.send(ORDER_EVENTS_TOPIC, String.valueOf(savedOrder.getId()), event);

        return savedOrder;
    }

    /**
     * 주문 상태 변경 후 Kafka 이벤트 발행
     * 유효하지 않은 전이 시 InvalidOrderStatusException 발생
     */
    public Order changeOrderStatus(Long orderId, OrderStatus targetStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        String previousStatus = order.getStatus().name();

        // 도메인 메서드로 상태 변경 (유효성 검증 포함)
        order.changeStatus(targetStatus);
        Order savedOrder = orderRepository.save(order);

        // 주문 상태 변경 이벤트 발행
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                savedOrder.getId(),
                savedOrder.getProductName(),
                previousStatus,
                savedOrder.getStatus().name()
        );
        kafkaTemplate.send(ORDER_STATUS_TOPIC, String.valueOf(savedOrder.getId()), event);

        return savedOrder;
    }

    /**
     * 주문 취소 후 재고 복원 및 Kafka 이벤트 발행 (보상 트랜잭션)
     * CREATED, CONFIRMED 상태에서만 취소 가능
     */
    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 도메인 메서드로 취소 (유효성 검증 포함)
        order.cancel();
        Order savedOrder = orderRepository.save(order);

        // 재고 복원 (보상 트랜잭션)
        if (order.getProductId() != null) {
            productRepository.findById(order.getProductId()).ifPresent(product -> {
                product.restoreStock(order.getQuantity());
                productRepository.save(product);
            });
        }

        // 주문 취소 이벤트 발행
        OrderCancelledEvent event = new OrderCancelledEvent(
                savedOrder.getId(),
                savedOrder.getProductId(),
                savedOrder.getProductName(),
                savedOrder.getQuantity()
        );
        kafkaTemplate.send(ORDER_CANCELLED_TOPIC, String.valueOf(savedOrder.getId()), event);

        return savedOrder;
    }

    /**
     * 주문 단건 상세 조회
     */
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /**
     * 최신 순으로 전체 주문 목록 조회
     */
    public List<Order> getOrders() {
        return orderRepository.findLatestOrders();
    }

    /**
     * 커서 기반 페이지네이션으로 주문 목록 조회
     * hasNext 판단을 위해 size + 1개를 조회한다.
     */
    public CursorPage<Order> getOrdersPaged(Long cursor, int size) {
        Pageable pageable = PageRequest.of(0, size + 1);

        List<Order> orders = (cursor == null)
                ? orderRepository.findLatestOrders(pageable)
                : orderRepository.findOrdersBefore(cursor, pageable);

        return CursorPage.of(orders, size, Order::getId);
    }

    /**
     * 검색/필터 + 커서 기반 페이지네이션으로 주문 목록 조회
     */
    public CursorPage<Order> searchOrders(Long cursor, int size,
                                           String keyword, OrderStatus status,
                                           LocalDate dateFrom, LocalDate dateTo) {
        Pageable pageable = PageRequest.of(0, size + 1);
        LocalDateTime from = (dateFrom != null) ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = (dateTo != null) ? dateTo.plusDays(1).atStartOfDay() : null;
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        List<Order> orders = (cursor == null)
                ? orderRepository.findOrdersByFilter(kw, status, from, to, pageable)
                : orderRepository.findOrdersByFilterBefore(cursor, kw, status, from, to, pageable);

        return CursorPage.of(orders, size, Order::getId);
    }
}
