package com.example.orderservice.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 오프셋 기반 페이지네이션 응답 DTO
 * @param content 현재 페이지 데이터
 * @param page 현재 페이지 번호 (0-based)
 * @param size 페이지 크기
 * @param totalElements 전체 데이터 수
 * @param totalPages 전체 페이지 수
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    /**
     * Spring Data Page 객체로부터 PageResponse 생성
     */
    public static <T> PageResponse<T> of(Page<T> springPage) {
        return new PageResponse<>(
                springPage.getContent(),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements(),
                springPage.getTotalPages()
        );
    }
}
