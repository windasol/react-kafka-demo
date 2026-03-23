# 빌드 및 실행

## 로컬 실행 순서

### 1. Kafka 실행 (Docker)
```bash
cd backend
docker-compose up -d
```

### 2. Order Service
```bash
cd backend/order-service
./gradlew bootRun
# 포트: 8080
```

### 3. Notification Service
```bash
cd backend/notification-service
./gradlew bootRun
# 포트: 8081
```

### 4. Frontend
```bash
cd frontend
npm install
npm run dev
# 포트: 5173
```

## Docker 전체 빌드 (배포용)
```bash
cd backend
docker-compose up --build
```

## 주의사항
- 코드 수정 시 Docker 재빌드 불필요 — `./gradlew bootRun` 재시작만
- Kafka는 항상 Docker로 실행 유지
- H2 콘솔: http://localhost:8081/h2-console (notification-service)

## API 엔드포인트

### Order Service (8080)
- `POST /api/orders` - 주문 생성
- `GET /api/orders` - 주문 목록 조회
- `PATCH /api/orders/{id}/status` - 주문 상태 변경 (CREATED → CONFIRMED → SHIPPED → DELIVERED)

### Notification Service (8081)
- `GET /api/notifications` - 알림 목록 조회
- `GET /api/notifications/stream` - SSE 실시간 스트림
- `PATCH /api/notifications/{id}/read` - 읽음 처리
- `PATCH /api/notifications/read-all` - 전체 읽음 처리
- `GET /api/notifications/unread-count` - 읽지 않은 알림 수

## Kafka 토픽
- `order-created` - 주문 생성 이벤트 (Order Service → Notification Service)
