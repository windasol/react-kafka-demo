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
  - `existsByOrderIdAndType(Long, NotificationType) → boolean` — 중복 이벤트 방지용

## Service
- `NotificationService`: Kafka 리스너 3개 (멱등성 처리 포함)
  - `order-events` → 중복 체크 후 ORDER_CREATED 알림 저장 + SSE broadcast
  - `order-status-events` → 중복 체크 후 상태 변경 알림 저장 + SSE broadcast
  - `order-cancelled-events` → 중복 체크 후 취소 알림 저장 + SSE broadcast
  - `low-stock-events` → LOW_STOCK 알림 저장 + SSE broadcast
- `SseEmitterService`
  - `createEmitter() → SseEmitter` (타임아웃 30분)
  - `broadcast(Notification)`

## Config
- `KafkaConsumerConfig`: 4개 토픽 모두 DLQ 적용 (FixedBackOff 1000ms × 3회 재시도 → `{토픽}.DLT`)

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

## Security
- `SecurityConfig`: JWT STATELESS. `/h2-console/**` permitAll
