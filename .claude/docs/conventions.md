# 코딩 컨벤션

## 공통
- 코드 설명 및 주석은 한국어
- 커밋 메시지는 영어, 명령형으로 작성 (예: `Add order cancel feature`)

---

## Java 핵심 설계 원칙

### 레이어드 아키텍처 (Layered Architecture)
계층 간 단방향 의존성을 유지한다. 상위 계층만 하위 계층을 참조한다.

```
Presentation Layer  (Controller)      ← HTTP 요청/응답 처리
        ↓
Business Layer      (Service)         ← 비즈니스 로직
        ↓
Persistence Layer   (Repository)      ← DB 접근
        ↓
Domain Layer        (Entity/VO)       ← 핵심 도메인 모델
```

- Controller는 Service만 호출한다. Repository를 직접 호출하지 않는다.
- Service는 비즈니스 로직만 담당한다. HTTP 관련 코드(`HttpServletRequest` 등) 금지.
- Repository는 데이터 접근만 담당한다. 비즈니스 로직을 포함하지 않는다.

---

### DDD (Domain-Driven Design, 도메인 주도 설계)
비즈니스 도메인 중심으로 코드를 구성한다.

- **Entity**: 고유 식별자(ID)를 가지는 도메인 객체 (`Order`, `Notification`)
- **Value Object**: 식별자 없이 값으로 구별되는 객체 (예: `Address`, `Money`)
- **Aggregate**: 연관된 Entity/VO의 묶음. 외부에서는 Aggregate Root를 통해서만 접근
- **Domain Event**: 도메인에서 발생한 사건을 표현 (`OrderCreatedEvent`)
- **Repository**: Aggregate 단위로 정의. 도메인 언어를 메서드명에 사용
  - 나쁜 예: `findByStatusAndCreatedAtBetween()`
  - 좋은 예: `findPendingOrdersInPeriod()`
- **Ubiquitous Language**: 팀 전체가 동일한 도메인 용어를 사용 (코드, 문서, 대화 모두 동일)

---

### SOLID 원칙

#### SRP (Single Responsibility Principle, 단일 책임 원칙)
하나의 클래스는 하나의 책임만 가진다.

```java
// 나쁜 예: OrderService가 알림까지 처리
public class OrderService {
    public void createOrder(...) {
        // 주문 저장
        // 이메일 발송  ← 알림 책임은 NotificationService가 담당해야 함
    }
}

// 좋은 예: 책임 분리
public class OrderService { ... }       // 주문 로직만
public class NotificationService { ... } // 알림 로직만
```

#### OCP (Open/Closed Principle, 개방-폐쇄 원칙)
확장에는 열려있고, 수정에는 닫혀있어야 한다.
- 새 기능 추가 시 기존 코드 수정 없이 인터페이스/추상 클래스를 구현하여 확장

#### LSP (Liskov Substitution Principle, 리스코프 치환 원칙)
자식 클래스는 부모 클래스를 대체할 수 있어야 한다.
- 상속보다 조합(Composition)을 우선 고려

#### ISP (Interface Segregation Principle, 인터페이스 분리 원칙)
클라이언트가 사용하지 않는 메서드에 의존하지 않도록 인터페이스를 분리한다.

#### DIP (Dependency Inversion Principle, 의존성 역전 원칙)
고수준 모듈은 저수준 모듈에 의존하지 않는다. 둘 다 추상화에 의존한다.
- 구현체가 아닌 인터페이스에 의존
- Spring의 생성자 주입 방식으로 준수

```java
// 나쁜 예: 구현체에 직접 의존
private final OrderRepositoryImpl orderRepository;

// 좋은 예: 인터페이스에 의존
private final OrderRepository orderRepository;
```

---

### 기타 Java 핵심 원칙

#### 불변 객체 우선 (Immutability First)
- 가능하면 `final` 필드 사용
- Setter 남용 금지 — 상태 변경은 도메인 메서드를 통해 표현
  ```java
  // 나쁜 예
  order.setStatus("CANCELLED");

  // 좋은 예
  order.cancel();
  ```

#### 의존성 주입 (Dependency Injection)
- 필드 주입(`@Autowired`) 금지, 생성자 주입 필수
- 테스트 용이성과 명시적 의존 관계를 위해

#### 방어적 프로그래밍
- 퍼블릭 메서드 입력값 검증 (`@Valid`)
- null 반환 금지 → `Optional<T>` 또는 빈 컬렉션 반환

#### Early Return (조기 반환)
중첩 조건문 대신 조기 반환으로 가독성과 성능을 높인다.
```java
// 나쁜 예
public String process(Order order) {
    if (order != null) {
        if (order.getStatus().equals("CREATED")) {
            return order.getProductName();
        }
    }
    return null;
}

// 좋은 예
public Optional<String> process(Order order) {
    if (ObjectUtils.isEmpty(order)) return Optional.empty();
    if (!order.getStatus().equals("CREATED")) return Optional.empty();
    return Optional.of(order.getProductName());
}
```

#### null 검사 — Apache Commons Lang3 사용
`null` 직접 비교 대신 Apache Commons의 유틸리티를 사용한다.
```java
// 나쁜 예
if (name == null || name.isEmpty()) { ... }
if (list == null || list.size() == 0) { ... }

// 좋은 예
if (StringUtils.isBlank(name)) { ... }        // 문자열
if (CollectionUtils.isEmpty(list)) { ... }     // 컬렉션
if (ObjectUtils.isEmpty(obj)) { ... }          // 객체/배열
```
- `StringUtils` — 문자열 null·공백·빈값 검사
- `CollectionUtils` — 컬렉션 null·empty 검사
- `ObjectUtils` — 일반 객체 null·empty 검사

#### 성능 최적화 & 메모리 절약
- **컬렉션 초기 용량 지정**: 크기를 알면 초기값 설정 → 불필요한 리사이징 방지
  ```java
  new ArrayList<>(items.size());
  new HashMap<>(16, 0.75f);
  ```
- **Stream 남용 금지**: 단순 반복은 for-each가 빠름. Stream은 복잡한 파이프라인에만 사용
- **불필요한 객체 생성 금지**: 루프 안에서 객체 생성 최소화
  ```java
  // 나쁜 예: 루프마다 StringBuilder 생성
  for (Order o : orders) { String s = new StringBuilder().append(o.getId()).toString(); }

  // 좋은 예: 재사용
  StringBuilder sb = new StringBuilder();
  for (Order o : orders) { sb.setLength(0); sb.append(o.getId()); }
  ```
- **`@Transactional` 범위 최소화**: DB 트랜잭션은 필요한 로직만 감싼다
- **N+1 문제 방지**: 연관 엔티티는 `fetch join` 또는 `@BatchSize`로 처리

#### 예외 처리
- 비즈니스 예외는 커스텀 예외 클래스로 정의
- `@ControllerAdvice`로 예외를 일관되게 처리
- 예외 메시지는 구체적으로 작성

#### 패키지 구조 (이 프로젝트)
```
com.example.{service}/
├── controller/    # Presentation Layer
├── service/       # Business Layer
├── repository/    # Persistence Layer
├── entity/        # Domain Model
├── event/         # Domain Event (Kafka)
└── config/        # 설정 클래스
```

---

## Frontend (React + TypeScript)
- 컴포넌트 파일명: PascalCase (`OrderForm.tsx`)
- 훅/유틸 파일명: camelCase (`orderApi.ts`)
- CSS 파일은 컴포넌트와 동일한 이름으로 (`OrderForm.css`)
- Props 인터페이스는 컴포넌트 파일 상단에 정의
- API 호출은 `src/api/` 디렉토리에 분리
- 타입 정의는 `src/types/index.ts`에 통합 관리

---

## Git 브랜치 전략
- `main` - 배포용
- `feature/기능명` - 기능 개발
- `fix/버그명` - 버그 수정
