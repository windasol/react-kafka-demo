# react-kafka-demo

React + Spring Boot + Apache Kafka 기반 주문/알림 데모 프로젝트

## 프로젝트 구조

```
react-kafka-demo/
├── frontend/                  # React + Vite + TypeScript
├── backend/
│   ├── order-service/         # 주문 생성 API (Spring Boot, 포트 8080)
│   └── notification-service/  # Kafka 소비 + SSE 알림 (Spring Boot, 포트 8081)
└── backend/docker-compose.yml # Kafka + Zookeeper
```

## 코드 수정 원칙

1. **코드 수정 전** 반드시 `.claude/docs/codebase-map.md`를 읽어 구조를 파악한다
2. codebase-map으로 수정 대상 파일을 특정한 뒤, **해당 파일만** Read → Edit 한다
3. 파일 전체 재작성(`Write`)보다 **부분 수정(`Edit`)을 우선** 사용한다
4. Bash 출력은 `| tail -n` 등으로 제한한다

## 참조 문서

필요할 때만 해당 문서를 읽어서 사용한다. (토큰 절약)

| 상황 | 문서 경로 |
|------|----------|
| 코드 수정 / 구조 파악 | `.claude/docs/codebase-map.md` |
| 빌드 / 실행 / API 확인 | `.claude/docs/build.md` |
| 코드 작성 / 리팩토링 | `.claude/docs/conventions.md` |
| 테스트 작성 / 실행 | `.claude/docs/testing.md` |
| 페이지네이션 | `.claude/docs/pagination.md` |

## 경로별 규칙 (자동 로딩)

- `.claude/rules/java.md` — `backend/**` 작업 시 자동 적용
- `.claude/rules/react.md` — `frontend/**` 작업 시 자동 적용
