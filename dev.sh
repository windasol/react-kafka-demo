#!/bin/bash

# 개발 모드 실행 스크립트
# Kafka만 Docker로, 나머지는 로컬에서 직접 실행

trap 'echo "종료 중..."; kill 0; exit' SIGINT SIGTERM

echo "=== 1. Kafka 시작 (Docker) ==="
cd backend && docker compose up -d
cd ..

echo "=== Kafka 준비 대기 중... ==="
until docker exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:29092 --list &>/dev/null; do
  sleep 2
  echo "  Kafka 아직 준비 안됨, 재시도..."
done
echo "=== Kafka 준비 완료 ==="

echo "=== 2. order-service 시작 ==="
cd backend/order-service && gradle bootRun &
ORDER_PID=$!
cd ../..

echo "=== 3. notification-service 시작 ==="
cd backend/notification-service && gradle bootRun &
NOTIF_PID=$!
cd ../..

echo "=== 4. Frontend 시작 (Vite dev server) ==="
cd frontend && npm run dev &
FRONT_PID=$!
cd ..

echo ""
echo "============================================"
echo "  Frontend:     http://localhost:5173"
echo "  Order API:    http://localhost:8081"
echo "  Notification: http://localhost:8082"
echo "  Kafka:        localhost:9092"
echo "============================================"
echo "  Ctrl+C 로 전체 종료"
echo "============================================"

wait
