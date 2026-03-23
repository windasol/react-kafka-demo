package com.example.orderservice.dto;

import java.util.List;
import java.util.function.Function;

/**
 * 커서 기반 페이지네이션 응답 DTO
 * @param content 현재 페이지 데이터
 * @param nextCursor 다음 페이지 커서 (null이면 마지막 페이지)
 * @param hasNext 다음 페이지 존재 여부
 */
public record CursorPage<T>(
        List<T> content,
        Long nextCursor,
        boolean hasNext
) {
    /**
     * 조회 결과로 CursorPage 생성
     * size + 1개를 조회한 뒤, 초과분이 있으면 hasNext = true로 판단한다.
     */
    public static <T> CursorPage<T> of(List<T> items, int size, Function<T, Long> idExtractor) {
        boolean hasNext = items.size() > size;
        List<T> content = hasNext ? items.subList(0, size) : items;
        Long nextCursor = hasNext ? idExtractor.apply(content.get(content.size() - 1)) : null;
        return new CursorPage<>(content, nextCursor, hasNext);
    }
}
