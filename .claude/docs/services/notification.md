# notification-service 상세

포트: 8082 | 패키지: `com.example.notificationservice`

## Entity
- `Notification`: id, orderId, type(NotificationType), message, isRead, createdAt
  - `create(orderId, type, message)`, `markAsRead()`
- `NotificationType`: enum ORDER_CREATED / CONFIRMED / SHIPPED / DELIVERED / CANCELLED

## Repository
- `NotificationRepository`
  - 커서 페이지네이션 쿼리
  - `countByIsReadFalse() → long`

## Service
- `NotificationService`: Kafka 리스너 3개
  - `order-events` → ORDER_CREATED 알림 저장 + SSE broadcast
  - `order-status-events` → 상태 변경 알림 저장 + SSE broadcast
  - `order-cancelled-events` → 취소 알림 저장 + SSE broadcast
- `SseEmitterService`
  - `createEmitter() → SseEmitter` (타임아웃 30분)
  - `broadcast(Notification)`

## Controller `GET|PATCH|DELETE /api/notifications`
- `GET /` → 전체 목록
- `GET /?paged&cursor&size=7` → 커서 페이지네이션
- `GET /stream` → SSE 스트림
- `PATCH /{id}/read` → 읽음 처리
- `PATCH /read-all` → 전체 읽음
- `DELETE /{id}` → 단건 삭제
- `DELETE /all` → 전체 삭제
- `GET /unread-count` → 미읽음 수

## DTO
- `CursorPage<T>`: content, nextCursor, hasNext

## Config
- `SecurityConfig`: JWT STATELESS. `/h2-console/**` permitAll
