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
- 퍼블릭 메서드 입력값 검증 (`Objects.requireNonNull`, `@Valid`)
- null 반환 금지 → `Optional<T>` 또는 빈 컬렉션 반환

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
