---
paths:
  - "frontend/**"
---

# React 프론트엔드 규칙

> 이 프로젝트는 **실제 운영 서비스** 기준으로 개발한다. "데모니까 괜찮다"는 없다.

---

## 네이밍 / 구조
- 컴포넌트: PascalCase (`OrderForm.tsx`), CSS 동일 이름 (`OrderForm.css`)
- 훅/유틸: camelCase (`orderApi.ts`)
- Props 인터페이스는 컴포넌트 파일 상단에 정의
- API 호출은 `src/api/`에 분리
- 타입 정의는 `src/types/index.ts`에 통합

---

## 페이지네이션
- 주문 목록: 오프셋 기반 + `Pagination` 컴포넌트 (7개 단위)
- 알림 목록: 커서 기반 + `useInfiniteScroll` 훅 사용
- 실시간 알림: SSE (`EventSource`) + 중복 방지

---

## 운영 관점 — 반드시 고려할 것

### API 에러 처리
- `try/catch` 없이 API 호출 금지 — 모든 비동기 호출은 에러 처리 필수
- 에러 상태를 UI에 표시 (토스트 또는 인라인 에러 메시지)
- 네트워크 오류와 비즈니스 오류 구분: `error.response?.data?.message ?? '일시적 오류가 발생했습니다.'` 패턴

### 로딩 / UX 상태
- API 호출 중 버튼 중복 클릭 방지 (`disabled={isLoading}`)
- 로딩 스피너 또는 스켈레톤 UI 표시 — 빈 화면 방치 금지
- 낙관적 업데이트 적용 시 실패 시 롤백 처리 필수

### 인증 / 보안
- JWT는 HttpOnly 쿠키로 관리 — `localStorage` / `sessionStorage` 토큰 저장 절대 금지
- 인증 필요한 페이지: 401 응답 시 자동 로그아웃 처리 (axiosConfig interceptor에서 처리)
- XSS 방지: `dangerouslySetInnerHTML` 사용 금지. 불가피하면 sanitize 후 사용
- 사용자 입력값을 URL에 직접 삽입 금지 — `encodeURIComponent` 처리

### 메모리 누수 방지
- SSE/타이머/리스너: useEffect cleanup에서 `es.close()` / `clearInterval()` / `removeEventListener()` 필수
- `AbortController`로 컴포넌트 unmount 시 진행 중인 fetch 취소
- useEffect dependency 누락으로 인한 stale closure 주의

### 중복 요청 방지
- 폼 제출, 주문 생성 등 **단발성 액션은 처리 중 재요청 차단**
- SSE 연결은 앱 전체에서 단일 인스턴스 유지 (Context 또는 전역 상태로 관리)

### 상태 관리
- 서버 데이터와 클라이언트 UI 상태 분리
- API 응답 데이터를 그대로 컴포넌트 state에 담지 말고 필요한 형태로 가공
- 전역 상태는 꼭 필요한 것만 (인증 정보, 알림 카운트 등) — 나머지는 로컬 state

### 접근성 / 안정성
- 빈 배열/null 렌더링 전 guard 처리 — optional chaining `?.` 적극 활용
- 숫자 포맷: 금액은 `toLocaleString()`, 날짜는 포맷 함수 통일
- 환경변수 누락 시 명확한 에러 발생 (`import.meta.env.VITE_XXX ?? throw`)

---

## 렌더링 / 안정성 규칙

### key prop
- 배열 index 금지, 도메인 고유 id 사용

### useEffect 의존성 배열 관리
- `exhaustive-deps` 경고 무시 금지
- 함수는 `useCallback`으로 참조 안정화 후 deps에 포함

### 타입 단언 (`as`) 사용 제한
- `as` 최소화: 타입 가드 함수 또는 타입 정의 보완으로 대체
- 불가피하게 사용 시 이유를 주석으로 명시
