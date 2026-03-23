# 페이지네이션

## 현재 상태

현재 주문/알림 목록은 전체 데이터를 한 번에 조회한다.

| 서비스 | 엔드포인트 | 현재 방식 |
|--------|-----------|-----------|
| order-service | `GET /api/orders` | `List<Order>` 전체 반환 |
| notification-service | `GET /api/notifications` | `List<Notification>` 전체 반환 |

데이터가 증가하면 응답 크기와 렌더링 비용이 선형으로 증가하므로, 페이지네이션 적용이 필요하다.

---

## 구현 전략: 커서 기반 페이지네이션 + 무한스크롤

주문/알림 모두 **최신순 정렬**이므로 커서 기반(Cursor-based) 페이지네이션이 적합하다.
오프셋 기반은 데이터 삽입/삭제 시 중복·누락이 발생할 수 있다.

### 왜 커서 기반인가?

| 방식 | 장점 | 단점 |
|------|------|------|
| 오프셋 (`?page=2&size=20`) | 구현 단순, 특정 페이지 이동 가능 | 실시간 데이터 삽입 시 중복/누락 발생 |
| 커서 (`?cursor=42&size=20`) | 실시간 데이터에 안전, 성능 우수 | 특정 페이지 점프 불가 |

이 프로젝트는 Kafka로 실시간 알림이 계속 들어오므로 **커서 기반**이 적합하다.

---

## Backend 변경 사항

### 1. 공통 응답 DTO

```java
// 커서 기반 페이지 응답 (두 서비스 공통)
public record CursorPage<T>(
    List<T> content,       // 현재 페이지 데이터
    Long nextCursor,       // 다음 페이지 커서 (null이면 마지막)
    boolean hasNext         // 다음 페이지 존재 여부
) {
    public static <T> CursorPage<T> of(List<T> items, int size,
                                        Function<T, Long> idExtractor) {
        boolean hasNext = items.size() > size;
        List<T> content = hasNext ? items.subList(0, size) : items;
        Long nextCursor = hasNext ? idExtractor.apply(content.get(content.size() - 1)) : null;
        return new CursorPage<>(content, nextCursor, hasNext);
    }
}
```

### 2. Order Service

**Repository** (`OrderRepository.java`)
```java
// 기존 - 전체 조회
@Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
List<Order> findLatestOrders();

// 추가 - 커서 기반 페이지네이션
@Query("SELECT o FROM Order o WHERE o.id < :cursor ORDER BY o.createdAt DESC")
List<Order> findOrdersBefore(@Param("cursor") Long cursor, Pageable pageable);

// 첫 페이지 (커서 없음)
@Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
List<Order> findLatestOrders(Pageable pageable);
```

**Service** (`OrderService.java`)
```java
private static final int DEFAULT_PAGE_SIZE = 20;

public CursorPage<Order> getOrdersPaged(Long cursor, int size) {
    int fetchSize = size + 1; // hasNext 판단용 1개 추가 조회
    Pageable pageable = PageRequest.of(0, fetchSize);

    List<Order> orders = (cursor == null)
        ? orderRepository.findLatestOrders(pageable)
        : orderRepository.findOrdersBefore(cursor, pageable);

    return CursorPage.of(orders, size, Order::getId);
}
```

**Controller** (`OrderController.java`)
```java
// 기존 엔드포인트 유지 (하위 호환)
@GetMapping
public ResponseEntity<List<Order>> getOrders() {
    return ResponseEntity.ok(orderService.getOrders());
}

// 페이지네이션 엔드포인트 추가
@GetMapping(params = "paged")
public ResponseEntity<CursorPage<Order>> getOrdersPaged(
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(orderService.getOrdersPaged(cursor, size));
}
```

### 3. Notification Service

**Repository** (`NotificationRepository.java`)
```java
// 기존 유지
@Query("SELECT n FROM Notification n ORDER BY n.createdAt DESC")
List<Notification> findLatestNotifications();

// 추가
@Query("SELECT n FROM Notification n WHERE n.id < :cursor ORDER BY n.createdAt DESC")
List<Notification> findNotificationsBefore(@Param("cursor") Long cursor, Pageable pageable);

@Query("SELECT n FROM Notification n ORDER BY n.createdAt DESC")
List<Notification> findLatestNotifications(Pageable pageable);
```

**Service** (`NotificationService.java`)
```java
private static final int DEFAULT_PAGE_SIZE = 20;

public CursorPage<Notification> getNotificationsPaged(Long cursor, int size) {
    int fetchSize = size + 1;
    Pageable pageable = PageRequest.of(0, fetchSize);

    List<Notification> notifications = (cursor == null)
        ? notificationRepository.findLatestNotifications(pageable)
        : notificationRepository.findNotificationsBefore(cursor, pageable);

    return CursorPage.of(notifications, size, Notification::getId);
}
```

**Controller** (`NotificationController.java`)
```java
@GetMapping(params = "paged")
public ResponseEntity<CursorPage<Notification>> getNotificationsPaged(
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(notificationService.getNotificationsPaged(cursor, size));
}
```

---

## Frontend 변경 사항

### 1. 타입 추가 (`types/index.ts`)

```typescript
/** 커서 기반 페이지 응답 */
export interface CursorPage<T> {
  content: T[];
  nextCursor: number | null;
  hasNext: boolean;
}
```

### 2. API 함수 변경

**`orderApi.ts`**
```typescript
// 기존 유지 (하위 호환)
export const fetchOrders = async (): Promise<Order[]> => { ... };

// 추가 - 페이지네이션
export const fetchOrdersPaged = async (cursor?: number, size = 20): Promise<CursorPage<Order>> => {
  const params = new URLSearchParams({ paged: 'true', size: String(size) });
  if (cursor) params.set('cursor', String(cursor));
  const response = await axios.get<CursorPage<Order>>(`${API_BASE}/api/orders?${params}`);
  return response.data;
};
```

**`notificationApi.ts`**
```typescript
export const fetchNotificationsPaged = async (cursor?: number, size = 20): Promise<CursorPage<Notification>> => {
  const params = new URLSearchParams({ paged: 'true', size: String(size) });
  if (cursor) params.set('cursor', String(cursor));
  const response = await axios.get<CursorPage<Notification>>(`${API_BASE}/api/notifications?${params}`);
  return response.data;
};
```

### 3. 무한스크롤 커스텀 훅 (`hooks/useInfiniteScroll.ts`)

```typescript
import { useEffect, useRef, useCallback } from 'react';

/** 스크롤이 하단에 도달하면 콜백을 실행하는 훅 */
export function useInfiniteScroll(onLoadMore: () => void, hasNext: boolean, isLoading: boolean) {
  const observerRef = useRef<IntersectionObserver | null>(null);
  const sentinelRef = useRef<HTMLDivElement | null>(null);

  const setSentinelRef = useCallback((node: HTMLDivElement | null) => {
    // 기존 Observer 정리
    if (observerRef.current) {
      observerRef.current.disconnect();
    }

    if (!node || !hasNext || isLoading) return;

    observerRef.current = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          onLoadMore();
        }
      },
      { threshold: 0.1 }
    );

    observerRef.current.observe(node);
    sentinelRef.current = node;
  }, [onLoadMore, hasNext, isLoading]);

  useEffect(() => {
    return () => observerRef.current?.disconnect();
  }, []);

  return setSentinelRef;
}
```

### 4. 컴포넌트 적용 예시 (`OrderList.tsx`)

```tsx
export default function OrderList({ refreshTrigger }: OrderListProps) {
  const [orders, setOrders] = useState<Order[]>([]);
  const [cursor, setCursor] = useState<number | null>(null);
  const [hasNext, setHasNext] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // 첫 페이지 로드 + 새로고침 시 초기화
  useEffect(() => {
    setOrders([]);
    setCursor(null);
    loadPage(null);
  }, [refreshTrigger]);

  const loadPage = async (nextCursor: number | null) => {
    setIsLoading(true);
    try {
      const page = await fetchOrdersPaged(nextCursor ?? undefined);
      setOrders((prev) => nextCursor ? [...prev, ...page.content] : page.content);
      setCursor(page.nextCursor);
      setHasNext(page.hasNext);
    } catch (err) {
      console.error('주문 목록 조회 실패:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const sentinelRef = useInfiniteScroll(
    () => loadPage(cursor),
    hasNext,
    isLoading
  );

  return (
    <div className="order-list">
      <h2>주문 목록</h2>
      {/* ... 기존 목록 렌더링 ... */}
      <ul>
        {orders.map((order) => (
          <li key={order.id}>{/* ... */}</li>
        ))}
      </ul>
      {/* 무한스크롤 감지 요소 */}
      <div ref={sentinelRef} className="scroll-sentinel" />
      {isLoading && <p className="loading">불러오는 중...</p>}
    </div>
  );
}
```

### 5. `NotificationList.tsx` 적용 시 주의사항

알림 목록은 SSE로 실시간 데이터가 추가되므로, 무한스크롤과 함께 처리해야 한다.

```tsx
// SSE로 들어오는 새 알림은 기존처럼 목록 상단에 추가
eventSource.addEventListener('notification', (event) => {
  const newNotification: Notification = JSON.parse(event.data);
  setNotifications((prev) => [newNotification, ...prev]);
  // 커서/hasNext는 변경하지 않음 (과거 데이터 로드와 독립)
});
```

---

## API 사용 예시

### 첫 페이지 조회
```bash
curl "http://localhost:8080/api/orders?paged&size=20"
```

응답:
```json
{
  "content": [
    { "id": 50, "productName": "노트북", "quantity": 1, "status": "CREATED", "createdAt": "..." },
    { "id": 49, "productName": "키보드", "quantity": 2, "status": "CONFIRMED", "createdAt": "..." }
  ],
  "nextCursor": 31,
  "hasNext": true
}
```

### 다음 페이지 조회
```bash
curl "http://localhost:8080/api/orders?paged&cursor=31&size=20"
```

### 알림 페이지네이션
```bash
curl "http://localhost:8081/api/notifications?paged&size=20"
curl "http://localhost:8081/api/notifications?paged&cursor=15&size=20"
```

---

## 변경 대상 파일 요약

### Backend

| 파일 | 변경 내용 |
|------|----------|
| `dto/CursorPage.java` (신규) | 커서 기반 페이지 응답 DTO |
| `OrderRepository.java` | 커서 기반 쿼리 메서드 추가 |
| `OrderService.java` | `getOrdersPaged()` 추가 |
| `OrderController.java` | `GET /api/orders?paged` 엔드포인트 추가 |
| `NotificationRepository.java` | 커서 기반 쿼리 메서드 추가 |
| `NotificationService.java` | `getNotificationsPaged()` 추가 |
| `NotificationController.java` | `GET /api/notifications?paged` 엔드포인트 추가 |

### Frontend

| 파일 | 변경 내용 |
|------|----------|
| `types/index.ts` | `CursorPage<T>` 인터페이스 추가 |
| `api/orderApi.ts` | `fetchOrdersPaged()` 추가 |
| `api/notificationApi.ts` | `fetchNotificationsPaged()` 추가 |
| `hooks/useInfiniteScroll.ts` (신규) | IntersectionObserver 기반 무한스크롤 훅 |
| `components/OrderList.tsx` | 무한스크롤 적용 |
| `components/NotificationList.tsx` | 무한스크롤 + SSE 병합 |
