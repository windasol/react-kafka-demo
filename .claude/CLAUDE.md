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

## 모델 전략 (Sonnet ↔ Haiku)

### Haiku 사용 — 단순·기계적 작업
- CSS 스타일 추가 / 수정
- import 누락 추가
- 오타·오명 수정 (rename, typo fix)
- 단일 필드 추가 (DTO, Entity)
- 로그 문자열·에러 메시지 변경
- 주석 추가

### Sonnet 사용 — 판단·설계가 필요한 작업
- 새 기능 구현 (API 엔드포인트, 컴포넌트 신규)
- 버그 원인 분석 및 수정
- 리팩터링 (구조 변경, 패턴 적용)
- 여러 파일에 걸친 연관 변경
- 보안·동시성 관련 코드
- 사용자 요청이 모호하여 판단이 필요한 경우

### 전환 명령어
```
/model claude-haiku-4-5-20251001   # Haiku로 전환
/model claude-sonnet-4-6           # Sonnet으로 복귀
```

## 기타 원칙
- Write보다 Edit 우선. Bash 출력은 `| tail -n` 제한
- 소스 파일 변경 후 반드시 관련 `.claude/docs/` 문서 업데이트 (커밋 전)

## 규칙 (자동 적용)
- `backend/**` → `.claude/rules/java.md`
- `frontend/**` → `.claude/rules/react.md`
