---
paths:
  - "backend/**"
---

# Java 백엔드 규칙

> 이 프로젝트는 **실제 운영 서비스** 기준으로 개발한다. "데모니까 괜찮다"는 없다.

---

## 레이어 아키텍처 (계층 건너뛰기 금지)
```
Controller → Service → Repository
```
- Controller: HTTP 입출력만. 비즈니스 로직 금지
- Service: 비즈니스 로직만. `HttpServletRequest` 등 HTTP 객체 금지
- Repository: 쿼리만. 비즈니스 판단 금지

---

## SRP — 단일 책임
- 클래스/메서드 하나당 역할 하나
- 메서드가 두 가지 이상을 한다면 분리

---

## DDD — 도메인 모델
- 상태 변경은 엔티티 메서드로 (`order.cancel()`, `product.deductStock()`)
- Setter 금지 — 도메인 의도를 드러내는 메서드명 사용
- 도메인 규칙(불변식)은 엔티티 내부에서 검증
- Kafka 이벤트는 `event/` 패키지에 도메인 이벤트로 정의
- **엔티티 `equals()`/`hashCode()` 주의**: JPA 엔티티에서 `@Id` 필드 기반으로만 구현하거나, Lombok `@EqualsAndHashCode` 사용 금지 (LazyLoading 필드 포함 시 N+1 유발)
  ```java
  // good — id 기반으로만 구현
  @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Order)) return false;
      Order other = (Order) o;
      return id != null && id.equals(other.getId());
  }
  @Override public int hashCode() { return getClass().hashCode(); }
  ```

---

## Early Return
- 중첩 if 대신 조기 반환으로 흐름 단순화
```java
// bad
if (user != null) { if (user.isActive()) { ... } }

// good
if (user == null) return;
if (!user.isActive()) return;
```

---

## Null 처리 — Apache Commons 사용
```java
StringUtils.isBlank(str)        // null·빈문자·공백 통합 체크
CollectionUtils.isEmpty(list)   // null·빈컬렉션 통합 체크
ObjectUtils.isEmpty(obj)        // null·빈객체·빈배열 통합 체크
```
- null 반환 금지 → `Optional<T>` 또는 빈 컬렉션 반환
- **`Optional.get()` 직접 호출 금지** — 반드시 `.orElseThrow()` / `.orElse()` / `.ifPresent()` 사용
  ```java
  // bad
  User user = userRepository.findById(id).get(); // NoSuchElementException 위험

  // good
  User user = userRepository.findById(id)
      .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
  ```

---

## DI / 트랜잭션
- 생성자 주입만 사용 (`@Autowired` 필드 주입 금지)
- `@Transactional` 범위 최소화 — 조회 전용은 `readOnly = true`
- 트랜잭션 내 외부 I/O(HTTP, Kafka 발행) 금지 — 트랜잭션 커밋 후 이벤트 발행
- **self-invocation 금지**: 같은 클래스 내 `this.method()` 호출은 Spring AOP 프록시를 우회하여 `@Transactional`이 적용되지 않음
  ```java
  // bad — this.로 호출하면 @Transactional 무시됨
  public void outer() { this.innerTransactional(); }
  @Transactional public void innerTransactional() { ... }

  // good — 별도 Service 빈으로 분리하거나 ApplicationContext에서 self-proxy 획득
  ```

---

## 예외 / 검증
- 입력 검증: `@Valid` + DTO 어노테이션
- 비즈니스 예외: 커스텀 예외 클래스 → `@ControllerAdvice`에서 일괄 처리
- 예외 메시지는 한국어
- 500 내부 오류는 외부에 스택트레이스 노출 금지 — 로그에만 기록

---

## 운영 관점 — 반드시 고려할 것

### 동시성
- 재고 차감, 주문 상태 변경 등 **경쟁 조건이 생기는 곳에는 반드시 낙관적/비관적 락 적용**
  ```java
  // 낙관적 락
  @Version private Long version;
  // 비관적 락
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Product> findWithLockById(Long id);
  ```
- `ConcurrentHashMap` 등 공유 자료구조는 thread-safe 구현체 사용 (일반 HashMap 금지)
- SSE Emitter 컬렉션 조작은 synchronized 또는 CopyOnWriteArrayList 사용

### 멱등성 / 중복 처리
- Kafka 컨슈머는 **at-least-once** 보장 → 동일 이벤트 중복 수신 가능
- 이벤트 처리는 orderId 등 고유 키 기준 **중복 체크 후 처리** (`processedEventIds` 또는 DB unique constraint 활용)

### 리소스 누수
- SSE Emitter: `onTimeout` / `onError` / `onCompletion` 콜백에서 반드시 컬렉션에서 제거
- DB 커넥션: 트랜잭션 범위 외부에서 엔티티 lazy 로딩 금지 (LazyInitializationException 방지)
- 파일/스트림: try-with-resources 사용

### 보안
- JWT는 HttpOnly 쿠키로만 전달 — Authorization 헤더·쿼리 파라미터·응답 바디에 토큰 노출 금지
- 사용자 리소스 접근 시 **소유권 검증 필수** — 타인의 주문/알림 접근 차단
  ```java
  // 반드시 username으로 소유권 확인
  Notification n = repository.findById(id).orElseThrow(...);
  if (!n.getUsername().equals(username)) throw new ForbiddenException("접근 권한이 없습니다.");
  ```
- SQL Injection: JPA 파라미터 바인딩 사용 (Native Query 직접 문자열 조합 금지)
- 민감 정보(비밀번호, 토큰)는 로그 출력 금지

### 성능
- N+1 문제: 연관 엔티티 조회 시 `@EntityGraph` 또는 fetch join 사용
- 대량 조회: 페이지네이션 없는 `findAll()` 금지 — 커서 또는 오프셋 페이징 필수
- 인덱스: 자주 조회하는 컬럼(`username`, `orderId`, `createdAt`)에 `@Index` 추가

### 장애 대응 (Kafka)
- Kafka 발행 실패 시 재시도 또는 DLQ(Dead Letter Queue) 처리 고려
- 컨슈머 예외는 반드시 catch → 로그 기록 후 nack 또는 건너뜀 처리 (서비스 전체 중단 방지)
  ```java
  try {
      notificationService.handleOrderCreatedEvent(event);
  } catch (Exception e) {
      log.error("이벤트 처리 실패 orderId={}", event.getOrderId(), e);
  }
  ```

### 로깅
- 운영 로그는 `log.info` / `log.warn` / `log.error` 레벨 구분
- 디버그용 `System.out.println` 절대 금지 — SLF4J Logger 사용
- 요청/응답 로그에 사용자 식별자(username) 포함
- 예외 로그: `log.error("메시지 orderId={}", id, e)` 형식 (파라미터 치환 + 예외 전달)
