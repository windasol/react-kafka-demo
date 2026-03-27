# jwt-common 상세

패키지: `com.example.jwtcommon`
경로: `backend/jwt-common/src/main/java/com/example/jwtcommon/`

## 클래스
- `JwtUtil`: HS256, 24시간 만료
  - `JwtUtil(secret, expirationMs)` — 생성자 주입 (Spring 빈은 JwtAutoConfiguration이 생성)
  - `generateToken(username) → String`
  - `extractUsername(token) → String`
  - `isValid(token) → boolean`

- `JwtAuthenticationFilter` (OncePerRequestFilter)
  - Authorization Bearer 헤더 또는 SSE 쿼리 파라미터(`?token=`)에서 JWT 추출
  - 유효 시 SecurityContext에 Authentication 설정

- `JwtSecurityConfigurer` (AbstractHttpConfigurer)
  - `http.with(jwtSecurityConfigurer, Customizer.withDefaults())`로 사용
  - JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 추가

- `JwtAutoConfiguration` (@AutoConfiguration)
  - `JwtUtil` 빈: `${jwt.secret}`, `${jwt.expiration-ms:86400000}` 바인딩
  - `JwtAuthenticationFilter` 빈
  - `JwtSecurityConfigurer` 빈

## Auto-configuration 등록
`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
```
com.example.jwtcommon.JwtAutoConfiguration
```

## 의존하는 서비스
`implementation project(':jwt-common')` — auth-service, order-service, notification-service
