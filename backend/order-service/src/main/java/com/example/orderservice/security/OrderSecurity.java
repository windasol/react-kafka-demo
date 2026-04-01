package com.example.orderservice.security;

import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Component;

/**
 * 주문 소유권 검증 Spring Security 표현식 빈.
 * @PreAuthorize("@orderSecurity.isOwner(#id, authentication.name)")으로 사용한다.
 * 주문이 존재하지 않으면 OrderNotFoundException(404), 소유자가 다르면 false 반환(403)
 */
@Component("orderSecurity")
public class OrderSecurity {

    private final OrderRepository orderRepository;

    public OrderSecurity(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public boolean isOwner(Long orderId, String username) {
        return orderRepository.findById(orderId)
                .map(order -> order.getUsername().equals(username))
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
