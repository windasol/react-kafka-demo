package com.example.orderservice.dto;

import com.example.orderservice.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * 주문 상태 변경 요청 DTO
 */
public record OrderStatusRequest(

        @NotNull(message = "변경할 상태는 필수입니다.")
        OrderStatus status
) {}
