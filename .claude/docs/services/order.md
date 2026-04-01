# order-service 상세

포트: 8083 | 패키지: `com.example.orderservice`

## Entity
- `Order`: id, username, productId, productName, quantity, unitPrice, status(OrderStatus), createdAt
  - `create(username, productId, productName, quantity, unitPrice)`
  - `changeStatus(OrderStatus)`, `confirm()`, `ship()`, `deliver()`, `cancel()`
- `Product`: id, name, price, stock, version(낙관적 락), createdAt
  - `create(name, price, stock)`, `update(name, price, stock)`
  - `deductStock(quantity) → boolean`, `restoreStock(quantity)`
- `OrderStatus`: enum CREATED→CONFIRMED→SHIPPED→DELIVERED, CREATED/CONFIRMED→CANCELLED
  - `canTransitionTo(OrderStatus) → boolean`, `isCancellable() → boolean`
- `OutboxEvent`: id, topic, messageKey, payload(TEXT), eventType, createdAt
  - `of(topic, messageKey, payload, eventType) → OutboxEvent` (정적 팩토리)

## Repository
- `OrderRepository`
  - `findAll(Pageable) → Page<Order>`
  - `searchByFilter(keyword, status, from, to, Pageable) → Page<Order>`
  - `findByIdLessThanOrderByIdDesc(...)` (커서)
- `ProductRepository`
  - `findAllByOrderByCreatedAtDesc()`
- `OutboxRepository`
  - `findTop100ByOrderByCreatedAtAsc() → List<OutboxEvent>`

## Service
- `OrderService`
  - `placeOrder(OrderRequest, username) → Order` — @Transactional 내부, Outbox 저장
  - `changeOrderStatus(Long, OrderStatus, username) → Order` — @Transactional, 소유권 검증, Outbox 저장
  - `cancelOrder(Long, username) → Order` — @Transactional, 소유권 검증, 재고 복원, Outbox 저장
  - `getOrder(Long, username) → Order` — 소유권 검증
  - `getOrdersPaged(page, size) → PageResponse`
  - `searchOrders(page, size, keyword, status, from, to) → PageResponse`
  - `getStatsSummary(username) → OrderStatsSummary` — totalRevenue·dailyStats.revenue 모두 CANCELLED 제외
  - `exportOrdersCsv(username) → String`
- `ProductService`
  - `createProduct`, `getProducts`, `getProduct`, `updateProduct`, `deleteProduct`
- `OutboxRelayService`
  - `relay()` — @Scheduled(fixedDelay=1000), Outbox → Kafka 발행 후 삭제

## Controller
- `OrderController` — `/api/orders`
  - POST `/` → 주문 생성
  - GET `/?paged&page=0&size=7` → 오프셋 페이지네이션
  - GET `/?search&page&keyword&status&dateFrom&dateTo` → 검색
  - GET `/{id}` → 상세
  - PATCH `/{id}/status` → 상태 변경
  - PATCH `/{id}/cancel` → 취소
  - GET `/stats` → OrderStatsSummary
  - GET `/export` → CSV 파일 다운로드
- `ProductController` — `/api/products` CRUD

## DTO
| 클래스 | 필드 |
|--------|------|
| `OrderRequest` | productId(@NotNull Long), quantity(@NotNull @Min(1)) |
| `ProductRequest` | name(@NotBlank), price(@Min(0)), stock(@Min(0)) |
| `OrderStatusRequest` | status(@NotNull OrderStatus) |
| `PageResponse<T>` | content, page, size, totalElements, totalPages |
| `CursorPage<T>` | content, nextCursor, hasNext |

## Kafka Events (발행)
| 이벤트 | 토픽 | 필드 |
|--------|------|------|
| `OrderCreatedEvent` | order-events | orderId, username, productName, quantity, status |
| `OrderStatusChangedEvent` | order-status-events | orderId, username, productName, previousStatus, newStatus |
| `OrderCancelledEvent` | order-cancelled-events | orderId, username, productId, productName, quantity |
| `LowStockEvent` | low-stock-events | productId, productName, remainingStock |

## Config / Exception
- `SecurityConfig`: JWT STATELESS. `/h2-console/**` permitAll
- `KafkaProducerConfig`: Kafka 프로듀서 설정
- `GlobalExceptionHandler`: OrderNotFound→404, ProductNotFound→404, InsufficientStock→409, InvalidOrderStatus→400, IllegalArgument→400, OptimisticLock→409, Forbidden→403
- `ForbiddenException`: 주문 소유권 불일치 시 발생 → 403
