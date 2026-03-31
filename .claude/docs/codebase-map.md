# 코드베이스 맵

| 서비스 | 포트   | 루트 패키지 | 상세 |
|--------|------|------------|------|
| auth-service | 8084 | `com.example.authservice` | `.claude/docs/services/auth.md` |
| order-service | 8083 | `com.example.orderservice` | `.claude/docs/services/order.md` |
| notification-service | 8082 | `com.example.notificationservice` | `.claude/docs/services/notification.md` |
| frontend | 5173 | `frontend/src` | `.claude/docs/services/frontend.md` |
| jwt-common | —    | `com.example.jwtcommon` | `.claude/docs/services/jwt-common.md` |

---

## auth-service
`backend/auth-service/src/main/java/com/example/authservice/`
```
entity/        User
repository/    UserRepository
service/       AuthService
controller/    AuthController
config/        SecurityConfig · KakaoOAuth2UserService · OAuth2SuccessHandler
dto/           AuthResponse · LoginRequest · RegisterRequest · FindUsernameRequest · ResetPasswordRequest
exception/     GlobalExceptionHandler
```
`backend/auth-service/src/main/resources/application.yml`

---

## order-service
`backend/order-service/src/main/java/com/example/orderservice/`
```
entity/        Order · Product · OrderStatus
repository/    OrderRepository · ProductRepository
service/       OrderService · ProductService
controller/    OrderController · ProductController
config/        SecurityConfig · KafkaProducerConfig
dto/           OrderRequest · ProductRequest · OrderStatusRequest · PageResponse · CursorPage
event/         OrderCreatedEvent · OrderStatusChangedEvent · OrderCancelledEvent
exception/     GlobalExceptionHandler · OrderNotFoundException · ProductNotFoundException
               InvalidOrderStatusException · InsufficientStockException
```
`backend/order-service/src/main/resources/application.yml`

---

## notification-service
`backend/notification-service/src/main/java/com/example/notificationservice/`
```
entity/        Notification · NotificationType
repository/    NotificationRepository
service/       NotificationService · SseEmitterService
controller/    NotificationController
config/        SecurityConfig
dto/           CursorPage
```
`backend/notification-service/src/main/resources/application.yml`

---

## frontend
`frontend/src/`
```
api/           authApi · orderApi · productApi · notificationApi · axiosConfig
components/    App · LoginPage · RegisterPage · FindAccountPage
               OrderList · OrderForm · OrderFilter · OrderDetail · Pagination
               ProductList · NotificationList
contexts/      AuthContext
hooks/         useInfiniteScroll
types/         index
```

---

## jwt-common
`backend/jwt-common/src/main/java/com/example/jwtcommon/`
```
JwtUtil · JwtAuthenticationFilter · JwtSecurityConfigurer · JwtAutoConfiguration
```
