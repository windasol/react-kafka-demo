# 테스트

## 현재 상태
- 테스트 코드 미구성 (데모 프로젝트)

## 수동 테스트 방법

### 주문 생성 (curl)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productName": "테스트상품", "quantity": 2}'
```

### 주문 목록 조회
```bash
curl http://localhost:8080/api/orders
```

### 주문 상태 변경 (CREATED → CONFIRMED → SHIPPED → DELIVERED)
```bash
# 주문 확인
curl -X PATCH http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "CONFIRMED"}'

# 배송 시작
curl -X PATCH http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "SHIPPED"}'

# 배송 완료
curl -X PATCH http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "DELIVERED"}'
```

### 알림 목록 조회
```bash
curl http://localhost:8081/api/notifications
```

### SSE 실시간 스트림 확인
```bash
curl -N http://localhost:8081/api/notifications/stream
```

### 스크립트 실행
```bash
# 프로젝트 루트의 테스트 스크립트
./test-notification.sh
```

## 테스트 추가 시 가이드

### Backend (JUnit 5 + Mockito)
```
backend/order-service/src/test/java/...
backend/notification-service/src/test/java/...
```
```bash
./gradlew test
```

### Frontend (Vitest)
```bash
cd frontend
npm run test
```

## Kafka 메시지 확인
```bash
# 컨테이너 접속 후 토픽 메시지 확인
docker exec -it kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic order-created \
  --from-beginning
```
