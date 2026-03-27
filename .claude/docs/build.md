# 빌드 및 실행

## 서비스 포트
| 서비스 | 포트 |
|--------|------|
| auth-service | 8080 |
| order-service | 8083 |
| notification-service | 8082 |
| frontend | 5173 |

## 로컬 실행 순서

```bash
# 1. Kafka (Docker)
cd backend && docker-compose up -d

# 2. auth-service
cd backend/auth-service && ./gradlew bootRun

# 3. order-service
cd backend/order-service && ./gradlew bootRun

# 4. notification-service
cd backend/notification-service && ./gradlew bootRun

# 5. frontend
cd frontend && npm install && npm run dev
```

## 주의사항
- 코드 수정 시 Docker 재빌드 불필요 — `./gradlew bootRun` 재시작만
- Kafka는 항상 Docker로 실행 유지
- H2 콘솔: `http://localhost:{port}/h2-console`
