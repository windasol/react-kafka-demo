# react-kafka-demo

React + Spring Boot + Apache Kafka 기반 주문/알림 데모 프로젝트

## 프로젝트 구조

```
react-kafka-demo/
├── frontend/                  # React + Vite + TypeScript
├── backend/
│   ├── order-service/         # 주문 생성 API (Spring Boot)
│   └── notification-service/  # Kafka 소비 + SSE 알림 (Spring Boot)
└── backend/docker-compose.yml # Kafka + Zookeeper
```

## 참조 문서

@.claude/docs/build.md
@.claude/docs/conventions.md
@.claude/docs/testing.md
