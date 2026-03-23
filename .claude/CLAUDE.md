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

필요할 때만 해당 문서를 읽어서 사용한다. (토큰 절약)

| 상황 | 문서 경로 |
|------|----------|
| 빌드 / 실행 / API 확인 | `.claude/docs/build.md` |
| 코드 작성 / 리팩토링 | `.claude/docs/conventions.md` |
| 테스트 작성 / 실행 | `.claude/docs/testing.md` |
| 페이지네이션 / 무한스크롤 | `.claude/docs/pagination.md` |
