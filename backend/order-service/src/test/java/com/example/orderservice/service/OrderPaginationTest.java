package com.example.orderservice.service;

import com.example.orderservice.dto.CursorPage;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

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
    @DisplayName("첫 페이지 조회: size개 반환, hasNext=true")
    void firstPage() {
        int size = 10;
        List<Order> items = orderRepository.findAllByOrderByIdDesc(PageRequest.of(0, size + 1));

        CursorPage<Order> page = CursorPage.of(items, size, Order::getId);

        assertThat(page.content()).hasSize(size);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.nextCursor()).isNotNull();
        // ID 내림차순 확인
        for (int i = 0; i < page.content().size() - 1; i++) {
            assertThat(page.content().get(i).getId())
                    .isGreaterThan(page.content().get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("두번째 페이지 조회: 커서 기반으로 이전 데이터 반환")
    void secondPage() {
        int size = 10;

        // 첫 페이지
        List<Order> firstItems = orderRepository.findAllByOrderByIdDesc(PageRequest.of(0, size + 1));
        CursorPage<Order> firstPage = CursorPage.of(firstItems, size, Order::getId);

        // 두번째 페이지
        List<Order> secondItems = orderRepository.findByIdLessThanOrderByIdDesc(
                firstPage.nextCursor(), PageRequest.of(0, size + 1));
        CursorPage<Order> secondPage = CursorPage.of(secondItems, size, Order::getId);

        assertThat(secondPage.content()).hasSize(size);
        assertThat(secondPage.hasNext()).isTrue();

        // 두번째 페이지의 최대 ID < 첫 페이지의 최소 ID
        Long firstPageMinId = firstPage.content().get(firstPage.content().size() - 1).getId();
        Long secondPageMaxId = secondPage.content().get(0).getId();
        assertThat(secondPageMaxId).isLessThan(firstPageMinId);
    }

    @Test
    @DisplayName("마지막 페이지: hasNext=false, nextCursor=null")
    void lastPage() {
        int size = 10;

        // 첫 페이지
        List<Order> firstItems = orderRepository.findAllByOrderByIdDesc(PageRequest.of(0, size + 1));
        CursorPage<Order> firstPage = CursorPage.of(firstItems, size, Order::getId);

        // 두번째 페이지
        List<Order> secondItems = orderRepository.findByIdLessThanOrderByIdDesc(
                firstPage.nextCursor(), PageRequest.of(0, size + 1));
        CursorPage<Order> secondPage = CursorPage.of(secondItems, size, Order::getId);

        // 세번째 (마지막) 페이지
        List<Order> thirdItems = orderRepository.findByIdLessThanOrderByIdDesc(
                secondPage.nextCursor(), PageRequest.of(0, size + 1));
        CursorPage<Order> thirdPage = CursorPage.of(thirdItems, size, Order::getId);

        assertThat(thirdPage.content()).hasSize(5); // 25 - 10 - 10 = 5
        assertThat(thirdPage.hasNext()).isFalse();
        assertThat(thirdPage.nextCursor()).isNull();
    }

    @Test
    @DisplayName("전체 페이지 순회: 중복 없이 모든 데이터 조회")
    void fullTraversal() {
        int size = 7;
        java.util.List<Long> allIds = new java.util.ArrayList<>();
        Long cursor = null;

        while (true) {
            List<Order> items = (cursor == null)
                    ? orderRepository.findAllByOrderByIdDesc(PageRequest.of(0, size + 1))
                    : orderRepository.findByIdLessThanOrderByIdDesc(cursor, PageRequest.of(0, size + 1));

            CursorPage<Order> page = CursorPage.of(items, size, Order::getId);
            page.content().forEach(o -> allIds.add(o.getId()));

            if (!page.hasNext()) break;
            cursor = page.nextCursor();
        }

        // 중복 없이 25개 모두 조회
        assertThat(allIds).hasSize(25);
        assertThat(allIds).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("빈 결과: hasNext=false, content 빈 리스트")
    void emptyResult() {
        orderRepository.deleteAll();
        List<Order> items = orderRepository.findAllByOrderByIdDesc(PageRequest.of(0, 11));
        CursorPage<Order> page = CursorPage.of(items, 10, Order::getId);

        assertThat(page.content()).isEmpty();
        assertThat(page.hasNext()).isFalse();
        assertThat(page.nextCursor()).isNull();
    }
}
