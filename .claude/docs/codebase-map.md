# 코드베이스 맵

## Order Service (backend/order-service, 포트 8080)

경로 접두사: `src/main/java/com/example/orderservice/`

### Entity
| 파일 | 핵심 |
|------|------|
| `entity/Order.java` | JPA. `create(Long,String,int,int)`, `changeStatus(OrderStatus)`, `confirm()`, `ship()`, `deliver()`, `cancel()` |
| `entity/Product.java` | JPA. `create(String,int,int)`, `update(String,int,int)`, `deductStock(int):boolean`, `restoreStock(int)` |
| `entity/OrderStatus.java` | enum. CREATED→CONFIRMED→SHIPPED→DELIVERED, CREATED/CONFIRMED→CANCELLED. `canTransitionTo()`, `isCancellable()` |

### Controller
| 파일 | 엔드포인트 |
|------|-----------|
| `controller/OrderController.java` | POST `/api/orders`, GET `/api/orders`, GET `?paged&page=0&size=7`, GET `?search&page&keyword&status&dateFrom&dateTo`, GET `/{id}`, PATCH `/{id}/status`, PATCH `/{id}/cancel` |
| `controller/ProductController.java` | POST/GET/GET{id}/PUT{id}/DELETE{id} `/api/products` |

### Service
| 파일 | 메서드 |
|------|--------|
| `service/OrderService.java` | `placeOrder(OrderRequest)→Order`, `changeOrderStatus(Long,OrderStatus)→Order`, `cancelOrder(Long)→Order`, `getOrdersPaged(int,int)→PageResponse`, `searchOrders(int,int,String,OrderStatus,LocalDate,LocalDate)→PageResponse` |
| `service/ProductService.java` | `createProduct`, `getProducts`, `getProduct`, `updateProduct`, `deleteProduct` |

### Repository
| 파일 | 핵심 쿼리 |
|------|----------|
| `repository/OrderRepository.java` | `findAll(Pageable)→Page`, `searchByFilter(keyword,status,from,to,Pageable)→Page`, 커서용: `findByIdLessThanOrderByIdDesc`, `findOrdersByFilter/Before` |
| `repository/ProductRepository.java` | `findAllByOrderByCreatedAtDesc()` |

### DTO
- `OrderRequest`: productId(@NotNull Long), quantity(@NotNull @Min(1) int)
- `ProductRequest`: name(@NotBlank), price(@Min(0) int), stock(@Min(0) int)
- `OrderStatusRequest`: status(@NotNull OrderStatus)
- `PageResponse<T>`: content, page, size, totalElements, totalPages. `of(Page)` 팩토리
- `CursorPage<T>`: content, nextCursor, hasNext. `of(List,int,Function)` 팩토리

### Event (Kafka 토픽)
- `OrderCreatedEvent` → `order-events`: orderId, productName, quantity, status
- `OrderStatusChangedEvent` → `order-status-events`: orderId, productName, previousStatus, newStatus
- `OrderCancelledEvent` → `order-cancelled-events`: orderId, productId, productName, quantity

### Exception / Config
- `GlobalExceptionHandler`: OrderNotFound(404), InvalidOrderStatus(400), ProductNotFound(404), InsufficientStock(409)
- `KafkaProducerConfig`, `CorsConfig`(localhost:5173,3000)

---

## Notification Service (backend/notification-service, 포트 8081)

경로 접두사: `src/main/java/com/example/notificationservice/`

| 레이어 | 파일 | 핵심 |
|--------|------|------|
| Entity | `entity/Notification.java` | `create(Long,NotificationType,String)`, `markAsRead()`. 필드: id,orderId,type,message,isRead,createdAt |
| Entity | `entity/NotificationType.java` | enum: ORDER_CREATED/CONFIRMED/SHIPPED/DELIVERED/CANCELLED |
| Controller | `controller/NotificationController.java` | GET `/api/notifications`, GET `?paged&cursor&size=7`, GET `/stream`(SSE), PATCH `/{id}/read`, PATCH `/read-all`, DELETE `/{id}`, DELETE `/all`, GET `/unread-count` |
| Service | `service/NotificationService.java` | Kafka 리스너 3개(order-events, order-status-events, order-cancelled-events). 페이지네이션: 커서 기반 |
| Service | `service/SseEmitterService.java` | `createEmitter()→SseEmitter`(30분), `broadcast(Notification)` |
| Repository | `repository/NotificationRepository.java` | 커서 페이지네이션, `countByIsReadFalse()` |
| DTO | `dto/CursorPage.java` | order-service와 동일 구조 |

---

## Frontend (frontend/src, 포트 5173)

### 타입 (types/index.ts)
- `Order`: id?, productId?, productName, quantity, unitPrice?, status?, createdAt?
- `Product`: id, name, price, stock, createdAt
- `Notification`: id, message, orderId, type, createdAt, isRead
- `PageResponse<T>`: content, page, size, totalElements, totalPages
- `CursorPage<T>`: content, nextCursor, hasNext
- 상수: `NEXT_STATUS`, `STATUS_LABEL`, `NOTIFICATION_ICON`, `NOTIFICATION_COLOR_CLASS`

### API (api/)
| 파일 | 함수 |
|------|------|
| `orderApi.ts` | `createOrder`, `fetchOrdersPaged(page,size)→PageResponse`, `searchOrders(params)→PageResponse`, `changeOrderStatus(id,status)`, `cancelOrder(id)` |
| `productApi.ts` | `fetchProducts`, `createProduct`, `updateProduct`, `deleteProduct` |
| `notificationApi.ts` | `fetchNotificationsPaged(cursor,size)→CursorPage`, `markAsRead`, `markAllAsRead`, `deleteNotification`, `deleteAllNotifications`, `getNotificationStreamUrl` |

### 컴포넌트 (components/)
| 파일 | Props | 기능 |
|------|-------|------|
| `OrderList.tsx` | refreshTrigger, onStockChanged? | 오프셋 페이지네이션(7개), 필터/검색, 상태 변경, 취소 |
| `OrderForm.tsx` | onOrderCreated, refreshProductTrigger | 상품 드롭다운, 수량 입력, 주문 생성 |
| `OrderFilter.tsx` | onFilter, onReset | keyword, status, dateFrom, dateTo 필터 |
| `OrderDetail.tsx` | orderId, onClose, onStatusChanged | 주문 상세 모달 |
| `Pagination.tsx` | currentPage, totalPages, onPageChange | « ‹ 1 2 3 › » 버튼 (최대 5개) |
| `ProductList.tsx` | onProductChanged, refreshTrigger | 상품 CRUD, 인라인 편집 |
| `NotificationList.tsx` | (없음) | 커서 무한스크롤, SSE 실시간, 읽음/삭제 |
| `App.tsx` | — | 루트. refreshTrigger/productTrigger로 컴포넌트 간 동기화 |

### 훅 (hooks/)
- `useInfiniteScroll.ts`: IntersectionObserver 기반. `(onLoadMore, hasNext, isLoading, rootRef?) → sentinelRef`
