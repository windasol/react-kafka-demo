package com.example.orderservice.service;

import com.example.orderservice.dto.PageResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderPaginationTest {

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        for (int i = 1; i <= 25; i++) {
            Order order = Order.create(1L, "상품" + i, i, 1000 * i);
            orderRepository.save(order);
        }
    }

    @Test
    @DisplayName("첫 페이지 조회: 7개 반환, totalPages=4")
    void firstPage() {
        Page<Order> page = orderRepository.findAll(
                PageRequest.of(0, 7, Sort.by(Sort.Direction.DESC, "id")));
        PageResponse<Order> response = PageResponse.of(page);

        assertThat(response.content()).hasSize(7);
        assertThat(response.page()).isZero();
        assertThat(response.totalElements()).isEqualTo(25);
        assertThat(response.totalPages()).isEqualTo(4); // ceil(25/7) = 4
        // ID 내림차순 확인
        for (int i = 0; i < response.content().size() - 1; i++) {
            assertThat(response.content().get(i).getId())
                    .isGreaterThan(response.content().get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("중간 페이지 조회: 7개 반환, 첫 페이지와 데이터 겹치지 않음")
    void middlePage() {
        Page<Order> firstPage = orderRepository.findAll(
                PageRequest.of(0, 7, Sort.by(Sort.Direction.DESC, "id")));
        Page<Order> secondPage = orderRepository.findAll(
                PageRequest.of(1, 7, Sort.by(Sort.Direction.DESC, "id")));

        PageResponse<Order> first = PageResponse.of(firstPage);
        PageResponse<Order> second = PageResponse.of(secondPage);

        assertThat(second.content()).hasSize(7);
        assertThat(second.page()).isEqualTo(1);

        // 두번째 페이지의 최대 ID < 첫 페이지의 최소 ID
        Long firstMinId = first.content().get(first.content().size() - 1).getId();
        Long secondMaxId = second.content().get(0).getId();
        assertThat(secondMaxId).isLessThan(firstMinId);
    }

    @Test
    @DisplayName("마지막 페이지: 나머지 4개만 반환")
    void lastPage() {
        Page<Order> page = orderRepository.findAll(
                PageRequest.of(3, 7, Sort.by(Sort.Direction.DESC, "id")));
        PageResponse<Order> response = PageResponse.of(page);

        assertThat(response.content()).hasSize(4); // 25 - 7*3 = 4
        assertThat(response.page()).isEqualTo(3);
        assertThat(response.totalPages()).isEqualTo(4);
    }

    @Test
    @DisplayName("전체 페이지 순회: 중복 없이 모든 데이터 조회")
    void fullTraversal() {
        java.util.List<Long> allIds = new java.util.ArrayList<>();
        int size = 7;

        for (int page = 0; ; page++) {
            Page<Order> result = orderRepository.findAll(
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
            PageResponse<Order> response = PageResponse.of(result);
            response.content().forEach(o -> allIds.add(o.getId()));

            if (page >= response.totalPages() - 1) break;
        }

        assertThat(allIds).hasSize(25);
        assertThat(allIds).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("빈 결과: content 빈 리스트, totalPages=0")
    void emptyResult() {
        orderRepository.deleteAll();
        Page<Order> page = orderRepository.findAll(
                PageRequest.of(0, 7, Sort.by(Sort.Direction.DESC, "id")));
        PageResponse<Order> response = PageResponse.of(page);

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isZero();
    }

    @Test
    @DisplayName("필터 검색: 상품명으로 필터링 + 페이지네이션")
    void searchByFilter() {
        // "상품1"로 검색하면 상품1, 상품10~19 = 11개 매칭, 2페이지
        Page<Order> page = orderRepository.searchByFilter(
                "상품1", null, null, null,
                PageRequest.of(0, 7, Sort.by(Sort.Direction.DESC, "id")));
        PageResponse<Order> response = PageResponse.of(page);

        assertThat(response.content()).hasSize(7);
        assertThat(response.totalElements()).isEqualTo(11);
        assertThat(response.totalPages()).isEqualTo(2);
        response.content().forEach(o ->
                assertThat(o.getProductName()).contains("상품1"));
    }
}
