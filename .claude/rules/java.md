---
paths:
  - "backend/**"
---

# Java 백엔드 규칙

- 레이어: Controller → Service → Repository (계층 건너뛰기 금지)
- Setter 금지 → 도메인 메서드 사용 (`order.cancel()`, `product.deductStock()`)
- 생성자 주입만 사용 (`@Autowired` 필드 주입 금지)
- null 처리: `StringUtils.isBlank()`, `CollectionUtils.isEmpty()`, `ObjectUtils.isEmpty()` (Apache Commons)
- null 반환 금지 → `Optional<T>` 또는 빈 컬렉션
- 입력 검증: `@Valid` 사용
- 예외: 커스텀 예외 + `@ControllerAdvice`
- `@Transactional` 범위 최소화
- Kafka 이벤트: `event/` 패키지에 도메인 이벤트 정의
