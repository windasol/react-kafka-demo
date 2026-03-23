package com.example.orderservice.service;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.entity.Order;
import com.example.orderservice.event.OrderCreatedEvent;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 주문 비즈니스 로직 담당 서비스
 * HTTP 관련 코드는 포함하지 않는다. (SRP 원칙)
 */
@Service
public class OrderService {

    private static final String ORDER_TOPIC = "order-events";

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderService(OrderRepository orderRepository,
                        KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 주문 생성 후 Kafka 이벤트 발행
     */
    public Order placeOrder(OrderRequest request) {
        Objects.requireNonNull(request, "주문 요청 정보는 필수입니다.");

        // 도메인 팩토리 메서드로 생성 (외부에서 Setter 사용 금지)
        Order order = Order.create(request.productName(), request.quantity());
        Order savedOrder = orderRepository.save(order);

        // 주문 생성 이벤트 발행
        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getProductName(),
                savedOrder.getQuantity(),
                savedOrder.getStatus()
        );
        kafkaTemplate.send(ORDER_TOPIC, String.valueOf(savedOrder.getId()), event);

        return savedOrder;
    }

    /**
     * 최신 순으로 전체 주문 목록 조회
     */
    public List<Order> getOrders() {
        return orderRepository.findLatestOrders();
    }
}
