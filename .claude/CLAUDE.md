# react-kafka-demo
React + Spring Boot + Kafka 주문/알림 데모

## 구조
`frontend/` | `backend/{auth-service·order-service·notification-service·jwt-common}/` | `backend/docker-compose.yml`

## 파일 읽기 순서 (MUST — 위반 금지)

1. **항상 먼저** `.claude/docs/codebase-map.md` 읽기 → 파일 경로 파악
2. **그 다음** `.claude/docs/services/{서비스}.md` 읽기 → 메서드/구조 파악
3. **마지막에만** 실제 소스 파일 Read → 그것도 수정 대상 파일만, 필요한 범위만

**절대 금지:**
- codebase-map.md / service docs를 읽지 않고 소스 파일 직접 Read 금지
- 소스 파일을 Read한 내용을 에이전트 프롬프트에 통째로 복사 금지
- 에이전트에게 위임할 작업을 미리 직접 Read 금지 (에이전트가 스스로 읽게 둘 것)
- Edit 1~2개짜리 단순 수정에 에이전트 사용 금지

## 참조 문서

| 상황 | 경로 |
|------|------|
| 파일 위치 파악 | `.claude/docs/codebase-map.md` |
| 서비스 API/메서드 상세 | `.claude/docs/services/{auth\|order\|notification\|frontend\|jwt-common}.md` |
| 포트 / 빌드 | `.claude/docs/build.md` |

## 기타 원칙
- Write보다 Edit 우선. Bash 출력은 `| tail -n` 제한

## 규칙 (자동 적용)
- `backend/**` → `.claude/rules/java.md`
- `frontend/**` → `.claude/rules/react.md`
