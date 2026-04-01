package com.example.orderservice.service;

import com.example.orderservice.dto.CursorPage;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderStatsSummary;
import com.example.orderservice.dto.PageResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.entity.Product;
import com.example.orderservice.event.LowStockEvent;
import com.example.orderservice.event.OrderCancelledEvent;
import com.example.orderservice.event.OrderCreatedEvent;
import com.example.orderservice.event.OrderStatusChangedEvent;
import com.example.orderservice.exception.ForbiddenException;
import com.example.orderservice.exception.InsufficientStockException;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.exception.ProductNotFoundException;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 주문 비즈니스 로직 담당 서비스
 * HTTP 관련 코드는 포함하지 않는다. (SRP 원칙)
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private static final String ORDER_EVENTS_TOPIC = "order-events";
    private static final String ORDER_STATUS_TOPIC = "order-status-events";
    private static final String ORDER_CANCELLED_TOPIC = "order-cancelled-events";
    private static final String LOW_STOCK_EVENTS_TOPIC = "low-stock-events";

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
     * 트랜잭션 커밋 후 Kafka 이벤트를 발행한다. (트랜잭션 내 외부 I/O 금지 원칙)
     */
    public Order placeOrder(OrderRequest request, String username) {
        Objects.requireNonNull(request, "주문 요청 정보는 필수입니다.");

        PlaceOrderResult result = placeOrderTransactional(request, username);

        kafkaTemplate.send(ORDER_EVENTS_TOPIC, String.valueOf(result.order().getId()),
                new OrderCreatedEvent(
                        result.order().getId(),
                        result.order().getUsername(),
                        result.order().getProductName(),
                        result.order().getQuantity(),
                        result.order().getStatus().name()
                ));

        if (result.lowStockEvent() != null) {
            kafkaTemplate.send(LOW_STOCK_EVENTS_TOPIC,
                    String.valueOf(result.lowStockEvent().getProductId()),
                    result.lowStockEvent());
            log.info("재고 부족 이벤트 발행 productId={} remainingStock={}",
                    result.lowStockEvent().getProductId(), result.lowStockEvent().getRemainingStock());
        }

        return result.order();
    }

    @Transactional
    protected PlaceOrderResult placeOrderTransactional(OrderRequest request, String username) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        if (!product.deductStock(request.quantity())) {
            throw new InsufficientStockException(
                    product.getName(), request.quantity(), product.getStock());
        }
        productRepository.save(product);

        Order order = Order.create(username, product.getId(), product.getName(), request.quantity(), product.getPrice());
        Order savedOrder = orderRepository.save(order);

        LowStockEvent lowStockEvent = null;
        if (LowStockEvent.isLowStock(product.getStock())) {
            lowStockEvent = new LowStockEvent(product.getId(), product.getName(), product.getStock());
        }

        return new PlaceOrderResult(savedOrder, lowStockEvent);
    }

    private record PlaceOrderResult(Order order, LowStockEvent lowStockEvent) {}

    /**
     * 주문 상태 변경 후 Kafka 이벤트 발행
     * 유효하지 않은 전이 시 InvalidOrderStatusException 발생
     */
    public Order changeOrderStatus(Long orderId, OrderStatus targetStatus, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getUsername().equals(username)) {
            throw new ForbiddenException("해당 주문에 대한 접근 권한이 없습니다.");
        }

        String previousStatus = order.getStatus().name();

        // 도메인 메서드로 상태 변경 (유효성 검증 포함)
        order.changeStatus(targetStatus);
        Order savedOrder = orderRepository.save(order);

        // 주문 상태 변경 이벤트 발행
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                savedOrder.getId(),
                savedOrder.getUsername(),
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
    public Order cancelOrder(Long orderId, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getUsername().equals(username)) {
            throw new ForbiddenException("해당 주문에 대한 접근 권한이 없습니다.");
        }

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
                savedOrder.getUsername(),
                savedOrder.getProductId(),
                savedOrder.getProductName(),
                savedOrder.getQuantity()
        );
        kafkaTemplate.send(ORDER_CANCELLED_TOPIC, String.valueOf(savedOrder.getId()), event);

        return savedOrder;
    }

    /**
     * 주문 단건 상세 조회
     * username 소유권을 검증한다. 불일치 시 ForbiddenException 발생
     */
    public Order getOrder(Long orderId, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!order.getUsername().equals(username)) {
            throw new ForbiddenException("해당 주문에 대한 접근 권한이 없습니다.");
        }
        return order;
    }

    /**
     * 최신 순으로 전체 주문 목록 조회
     */
    public List<Order> getOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 오프셋 기반 페이지네이션으로 주문 목록 조회
     */
    public PageResponse<Order> getOrdersPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Order> result = orderRepository.findAll(pageable);
        return PageResponse.of(result);
    }

    /**
     * 검색/필터 + 오프셋 기반 페이지네이션으로 주문 목록 조회
     */
    public PageResponse<Order> searchOrders(int page, int size,
                                             String keyword, OrderStatus status,
                                             LocalDate dateFrom, LocalDate dateTo) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        LocalDateTime from = (dateFrom != null) ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = (dateTo != null) ? dateTo.plusDays(1).atStartOfDay() : null;
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        Page<Order> result = orderRepository.searchByFilter(kw, status, from, to, pageable);
        return PageResponse.of(result);
    }

    /**
     * 사용자별 주문 통계 요약 조회 (최근 7일 일별 통계 포함)
     */
    @Transactional(readOnly = true)
    public OrderStatsSummary getStatsSummary(String username) {
        long totalOrders = orderRepository.countByUsernameAndStatus(username, OrderStatus.CREATED)
                + orderRepository.countByUsernameAndStatus(username, OrderStatus.CONFIRMED)
                + orderRepository.countByUsernameAndStatus(username, OrderStatus.SHIPPED)
                + orderRepository.countByUsernameAndStatus(username, OrderStatus.DELIVERED)
                + orderRepository.countByUsernameAndStatus(username, OrderStatus.CANCELLED);

        long pendingOrders = orderRepository.countByUsernameAndStatus(username, OrderStatus.CREATED)
                + orderRepository.countByUsernameAndStatus(username, OrderStatus.CONFIRMED)
                + orderRepository.countByUsernameAndStatus(username, OrderStatus.SHIPPED);

        long completedOrders = orderRepository.countByUsernameAndStatus(username, OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByUsernameAndStatus(username, OrderStatus.CANCELLED);

        List<Order> deliveredOrders = orderRepository.findByUsernameOrderByCreatedAtDesc(username)
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .toList();

        double totalRevenue = deliveredOrders.stream()
                .mapToDouble(o -> (double) o.getUnitPrice() * o.getQuantity())
                .sum();

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> rawDailyStats = orderRepository.findDailyStatsByUsername(username, sevenDaysAgo);

        List<OrderStatsSummary.DailyStat> dailyStats = new ArrayList<>();
        for (Object[] row : rawDailyStats) {
            String date = row[0] != null ? row[0].toString() : "";
            long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            double revenue = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            dailyStats.add(new OrderStatsSummary.DailyStat(date, count, revenue));
        }

        return new OrderStatsSummary(totalOrders, pendingOrders, completedOrders, cancelledOrders,
                totalRevenue, dailyStats);
    }

    /**
     * 사용자별 전체 주문 CSV 문자열 반환
     * 헤더: 주문번호,상품명,수량,금액,상태,주문일시
     */
    @Transactional(readOnly = true)
    public String exportOrdersCsv(String username) {
        List<Order> orders = orderRepository.findByUsernameOrderByCreatedAtDesc(username);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        StringBuilder sb = new StringBuilder();
        sb.append("주문번호,상품명,수량,금액,상태,주문일시\n");
        for (Order order : orders) {
            sb.append(order.getId()).append(",")
              .append(escapeCsvField(order.getProductName())).append(",")
              .append(order.getQuantity()).append(",")
              .append(order.getUnitPrice() != null ? (long) order.getUnitPrice() * order.getQuantity() : 0).append(",")
              .append(order.getStatus().name()).append(",")
              .append(order.getCreatedAt() != null ? order.getCreatedAt().format(formatter) : "")
              .append("\n");
        }
        return sb.toString();
    }

    private String escapeCsvField(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
