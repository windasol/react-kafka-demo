# 페이지네이션

커서 기반(Cursor-based) 페이지네이션 + 무한스크롤로 구현되어 있다.
기본 페이지 사이즈: **7**

## API 엔드포인트

### Order Service (8080)
```
GET /api/orders?paged&size=7              # 첫 페이지
GET /api/orders?paged&cursor={id}&size=7  # 다음 페이지
GET /api/orders?search&keyword=...&status=...&dateFrom=...&dateTo=...  # 검색+페이징
```

### Notification Service (8081)
```
GET /api/notifications?paged&size=7
GET /api/notifications?paged&cursor={id}&size=7
```

## 응답 구조 — `CursorPage<T>`
```json
{
  "content": [...],
  "nextCursor": 31,      // null이면 마지막 페이지
  "hasNext": true
}
```

## 구현 위치
- Backend DTO: `dto/CursorPage.java` (order-service, notification-service 각각)
- Frontend 타입: `types/index.ts` → `CursorPage<T>`
- Frontend 훅: `hooks/useInfiniteScroll.ts` (IntersectionObserver)
- Frontend API: `api/orderApi.ts`, `api/notificationApi.ts`
