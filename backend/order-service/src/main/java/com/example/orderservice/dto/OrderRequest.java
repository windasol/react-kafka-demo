package com.example.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 주문 생성 요청 DTO
 * Presentation Layer에서 도메인 Entity를 직접 노출하지 않기 위해 사용한다.
 */
public record OrderRequest(

        @NotBlank(message = "상품명은 필수입니다.")
        String productName,

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        Integer quantity
) {}
