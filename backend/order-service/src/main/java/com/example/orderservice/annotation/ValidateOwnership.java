package com.example.orderservice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 주문 소유권 검증 어노테이션.
 * 메서드 파라미터에 orderId(Long)와 username(String)이 있어야 한다.
 * OwnershipAspect가 호출 전 DB에서 주문을 조회하여 username 일치 여부를 검증한다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateOwnership {
}
