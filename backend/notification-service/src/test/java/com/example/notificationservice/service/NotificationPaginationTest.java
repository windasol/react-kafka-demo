package com.example.notificationservice.service;

import com.example.notificationservice.dto.CursorPage;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.entity.NotificationType;
import com.example.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificationPaginationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        for (int i = 1; i <= 25; i++) {
            Notification notification = Notification.create(
                    (long) i, NotificationType.ORDER_CREATED, "알림 메시지 " + i);
            notificationRepository.save(notification);
        }
    }

    @Test
    @DisplayName("첫 페이지 조회: size개 반환, hasNext=true")
    void firstPage() {
        int size = 10;
        List<Notification> items = notificationRepository.findAllByOrderByIdDesc(PageRequest.of(0, size + 1));
        CursorPage<Notification> page = CursorPage.of(items, size, Notification::getId);

        assertThat(page.content()).hasSize(size);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.nextCursor()).isNotNull();
    }

    @Test
    @DisplayName("커서 기반 두번째 페이지: 이전 데이터 없이 다음 데이터 반환")
    void secondPage() {
        int size = 10;

        List<Notification> firstItems = notificationRepository.findAllByOrderByIdDesc(PageRequest.of(0, size + 1));
        CursorPage<Notification> firstPage = CursorPage.of(firstItems, size, Notification::getId);

        List<Notification> secondItems = notificationRepository.findByIdLessThanOrderByIdDesc(
                firstPage.nextCursor(), PageRequest.of(0, size + 1));
        CursorPage<Notification> secondPage = CursorPage.of(secondItems, size, Notification::getId);

        assertThat(secondPage.content()).hasSize(size);
        // 중복 없음 확인
        List<Long> firstIds = firstPage.content().stream().map(Notification::getId).toList();
        List<Long> secondIds = secondPage.content().stream().map(Notification::getId).toList();
        assertThat(firstIds).doesNotContainAnyElementsOf(secondIds);
    }

    @Test
    @DisplayName("전체 순회: 중복 없이 모든 데이터 조회")
    void fullTraversal() {
        int size = 7;
        List<Long> allIds = new ArrayList<>();
        Long cursor = null;

        while (true) {
            List<Notification> items = (cursor == null)
                    ? notificationRepository.findAllByOrderByIdDesc(PageRequest.of(0, size + 1))
                    : notificationRepository.findByIdLessThanOrderByIdDesc(cursor, PageRequest.of(0, size + 1));

            CursorPage<Notification> page = CursorPage.of(items, size, Notification::getId);
            page.content().forEach(n -> allIds.add(n.getId()));

            if (!page.hasNext()) break;
            cursor = page.nextCursor();
        }

        assertThat(allIds).hasSize(25);
        assertThat(allIds).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("마지막 페이지: hasNext=false")
    void lastPage() {
        int size = 20;
        List<Notification> firstItems = notificationRepository.findAllByOrderByIdDesc(PageRequest.of(0, size + 1));
        CursorPage<Notification> firstPage = CursorPage.of(firstItems, size, Notification::getId);

        List<Notification> secondItems = notificationRepository.findByIdLessThanOrderByIdDesc(
                firstPage.nextCursor(), PageRequest.of(0, size + 1));
        CursorPage<Notification> secondPage = CursorPage.of(secondItems, size, Notification::getId);

        assertThat(secondPage.content()).hasSize(5);
        assertThat(secondPage.hasNext()).isFalse();
        assertThat(secondPage.nextCursor()).isNull();
    }
}
