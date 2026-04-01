package com.example.orderservice.aspect;

import com.example.orderservice.entity.Order;
import com.example.orderservice.exception.ForbiddenException;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.repository.OrderRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Component;

/**
 * 주문 소유권 검증 AOP.
 * @ValidateOwnership 어노테이션이 선언된 메서드 실행 전에 intercept하여
 * orderId 파라미터로 주문을 조회하고 username이 일치하는지 검증한다.
 */
@Aspect
@Component
public class OwnershipAspect {

    private final OrderRepository orderRepository;
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    public OwnershipAspect(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Before("@annotation(com.example.orderservice.annotation.ValidateOwnership)")
    public void checkOwnership(JoinPoint jp) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        String[] paramNames = nameDiscoverer.getParameterNames(signature.getMethod());
        Object[] args = jp.getArgs();

        Long orderId = extractParam(paramNames, args, "orderId", Long.class);
        String username = extractParam(paramNames, args, "username", String.class);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getUsername().equals(username)) {
            throw new ForbiddenException("해당 주문에 대한 접근 권한이 없습니다.");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T extractParam(String[] names, Object[] args, String targetName, Class<T> type) {
        if (names == null) {
            throw new IllegalStateException(
                    "@ValidateOwnership: 파라미터 이름을 확인할 수 없습니다. -parameters 컴파일 옵션을 확인하세요.");
        }
        for (int i = 0; i < names.length; i++) {
            if (targetName.equals(names[i]) && type.isInstance(args[i])) {
                return (T) args[i];
            }
        }
        throw new IllegalStateException(
                "@ValidateOwnership: '" + targetName + "' 파라미터를 찾을 수 없습니다.");
    }
}
