---
paths:
  - "frontend/**"
---

# React 프론트엔드 규칙

- 컴포넌트: PascalCase (`OrderForm.tsx`), CSS 동일 이름 (`OrderForm.css`)
- 훅/유틸: camelCase (`orderApi.ts`)
- Props 인터페이스는 컴포넌트 파일 상단에 정의
- API 호출은 `src/api/`에 분리
- 타입 정의는 `src/types/index.ts`에 통합
- 주문 목록 페이지네이션: 오프셋 기반 + `Pagination` 컴포넌트 (7개 단위)
- 알림 목록 페이지네이션: 커서 기반 + `useInfiniteScroll` 훅 사용
- 실시간 알림: SSE (`EventSource`) + 중복 방지
