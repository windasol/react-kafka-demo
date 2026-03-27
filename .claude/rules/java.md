---
paths:
  - "backend/**"
---

# Java 백엔드 규칙

## 레이어 아키텍처 (계층 건너뛰기 금지)
```
Controller → Service → Repository
```
- Controller: HTTP 입출력만. 비즈니스 로직 금지
- Service: 비즈니스 로직만. `HttpServletRequest` 등 HTTP 객체 금지
- Repository: 쿼리만. 비즈니스 판단 금지

## SRP — 단일 책임
- 클래스/메서드 하나당 역할 하나
- 메서드가 두 가지 이상을 한다면 분리

## DDD — 도메인 모델
- 상태 변경은 엔티티 메서드로 (`order.cancel()`, `product.deductStock()`)
- Setter 금지 — 도메인 의도를 드러내는 메서드명 사용
- 도메인 규칙(불변식)은 엔티티 내부에서 검증
- Kafka 이벤트는 `event/` 패키지에 도메인 이벤트로 정의

## Early Return
- 중첩 if 대신 조기 반환으로 흐름 단순화
```java
// bad
if (user != null) {
    if (user.isActive()) { ... }
}

// good
if (user == null) return;
if (!user.isActive()) return;
...
```

## Null 처리 — Apache Commons 사용
```java
StringUtils.isBlank(str)        // null·빈문자·공백 통합 체크
CollectionUtils.isEmpty(list)   // null·빈컬렉션 통합 체크
ObjectUtils.isEmpty(obj)        // null·빈객체·빈배열 통합 체크
```
- null 반환 금지 → `Optional<T>` 또는 빈 컬렉션 반환

## DI / 트랜잭션
- 생성자 주입만 사용 (`@Autowired` 필드 주입 금지)
- `@Transactional` 범위 최소화 — 조회 전용은 `readOnly = true`

## 예외 / 검증
- 입력 검증: `@Valid` + DTO 어노테이션
- 비즈니스 예외: 커스텀 예외 클래스 → `@ControllerAdvice`에서 일괄 처리
- 예외 메시지는 한국어
