# 코딩 컨벤션

## 공통
- 코드 설명 및 주석은 한국어
- 커밋 메시지는 영어, 명령형으로 작성 (예: `Add order cancel feature`)

---

## Java (Spring Boot)

### 아키텍처 규칙
- Controller → Service만 호출 (Repository 직접 참조 금지)
- Service에 HTTP 관련 코드 금지 (`HttpServletRequest` 등)
- Setter 금지 → 도메인 메서드 사용 (`order.cancel()`, `product.deductStock()`)
- 필드 주입(`@Autowired`) 금지, 생성자 주입 필수
- `@Transactional` 범위 최소화

### null 처리 — Apache Commons Lang3 사용
```java
StringUtils.isBlank(name)          // 문자열
CollectionUtils.isEmpty(list)      // 컬렉션
ObjectUtils.isEmpty(obj)           // 객체/배열
```
- null 반환 금지 → `Optional<T>` 또는 빈 컬렉션 반환

### 예외 처리
- 비즈니스 예외는 커스텀 예외 클래스로 정의
- `@ControllerAdvice`로 일관 처리
- 퍼블릭 메서드 입력값 검증 (`@Valid`)

### 패키지 구조
```
com.example.{service}/
├── controller/    # Presentation
├── service/       # Business
├── repository/    # Persistence
├── entity/        # Domain Model
├── dto/           # Data Transfer Object
├── event/         # Domain Event (Kafka)
├── exception/     # 커스텀 예외
└── config/        # 설정
```

---

## Frontend (React + TypeScript)
- 컴포넌트: PascalCase (`OrderForm.tsx`), CSS 동일 이름 (`OrderForm.css`)
- 훅/유틸: camelCase (`orderApi.ts`)
- Props 인터페이스는 컴포넌트 파일 상단에 정의
- API 호출은 `src/api/`에 분리, 타입은 `src/types/index.ts`에 통합

---

## Git 브랜치 전략
- `main` — 배포용
- `feature/기능명` — 기능 개발
- `fix/버그명` — 버그 수정
