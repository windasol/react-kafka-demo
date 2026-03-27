# order-service 상세

포트: 8083 | 패키지: `com.example.orderservice`

## Entity
- `Order`: id, userId, productName, quantity, unitPrice, status(OrderStatus), createdAt
  - `create(userId, productName, quantity, unitPrice)`
  - `changeStatus(OrderStatus)`, `confirm()`, `ship()`, `deliver()`, `cancel()`
- `Product`: id, name, price, stock, createdAt
  - `create(name, price, stock)`, `update(name, price, stock)`
  - `deductStock(quantity) → boolean`, `restoreStock(quantity)`
- `OrderStatus`: enum CREATED→CONFIRMED→SHIPPED→DELIVERED, CREATED/CONFIRMED→CANCELLED
  - `canTransitionTo(OrderStatus) → boolean`, `isCancellable() → boolean`

## Repository
- `OrderRepository`
  - `findAll(Pageable) → Page<Order>`
  - `searchByFilter(keyword, status, from, to, Pageable) → Page<Order>`
  - `findByIdLessThanOrderByIdDesc(...)` (커서)
- `ProductRepository`
  - `findAllByOrderByCreatedAtDesc()`

## Service
- `OrderService`
  - `placeOrder(OrderRequest) → Order`
  - `changeOrderStatus(Long, OrderStatus) → Order`
  - `cancelOrder(Long) → Order`
  - `getOrdersPaged(page, size) → PageResponse`
  - `searchOrders(page, size, keyword, status, from, to) → PageResponse`
- `ProductService`
  - `createProduct`, `getProducts`, `getProduct`, `updateProduct`, `deleteProduct`

## Controller
- `OrderController` — `/api/orders`
  - POST `/` → 주문 생성
  - GET `/?paged&page=0&size=7` → 오프셋 페이지네이션
  - GET `/?search&page&keyword&status&dateFrom&dateTo` → 검색
  - GET `/{id}` → 상세
  - PATCH `/{id}/status` → 상태 변경
  - PATCH `/{id}/cancel` → 취소
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
| `OrderCreatedEvent` | order-events | orderId, productName, quantity, status |
| `OrderStatusChangedEvent` | order-status-events | orderId, productName, previousStatus, newStatus |
| `OrderCancelledEvent` | order-cancelled-events | orderId, productId, productName, quantity |

## Config / Exception
- `SecurityConfig`: JWT STATELESS. `/h2-console/**` permitAll
- `KafkaProducerConfig`: Kafka 프로듀서 설정
- `GlobalExceptionHandler`: OrderNotFound→404, ProductNotFound→404, InsufficientStock→409, InvalidOrderStatus→400, IllegalArgument→400
