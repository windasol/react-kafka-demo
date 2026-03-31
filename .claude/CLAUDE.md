# react-kafka-demo
React + Spring Boot + Kafka 주문/알림 데모

## 구조
`frontend/` | `backend/{auth-service·order-service·notification-service·jwt-common}/` | `backend/docker-compose.yml`

## 원칙
- 수정 전 `codebase-map.md`로 경로 파악 → 해당 파일만 Read → Edit
- Write보다 Edit 우선. Bash 출력은 `| tail -n` 제한

## 참조 문서 (필요할 때만 Read)

| 상황 | 경로 |
|------|------|
| 파일 위치 파악 | `.claude/docs/codebase-map.md` |
| 서비스 API/메서드 상세 | `.claude/docs/services/{auth\|order\|notification\|frontend\|jwt-common}.md` |
| 포트 / 빌드 | `.claude/docs/build.md` |

## 규칙 (자동 적용)
- `backend/**` → `.claude/rules/java.md`
- `frontend/**` → `.claude/rules/react.md`
