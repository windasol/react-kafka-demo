# auth-service 상세

포트: 8084 | 패키지: `com.example.authservice`

## Entity
- `User`: id, username(unique), password(BCrypt), email, provider, createdAt
  - `create(username, encodedPassword, email)`
  - `createOAuth(username, email, provider)`
  - `changePassword(encodedPassword)`

## Repository
- `UserRepository`
  - `findByUsername(String) → Optional<User>`
  - `existsByUsername(String) → boolean`
  - `findByEmail(String) → Optional<User>`
  - `findByUsernameAndEmail(String, String) → Optional<User>`

## Service
- `AuthService`
  - `register(RegisterRequest) → AuthResponse`
  - `login(LoginRequest) → AuthResponse`
  - `findUsername(FindUsernameRequest) → String`
  - `resetPassword(ResetPasswordRequest) → void`

## Controller `POST /api/auth`
- `/register` → AuthResponse
- `/login` → AuthResponse
- `/find-username` → `{username}`
- `/reset-password` → `{message}`

## DTO
| 클래스 | 필드 |
|--------|------|
| `LoginRequest` | username(@NotBlank), password(@NotBlank @Size≥4) |
| `RegisterRequest` | username(@Size 3-20), password(@Size 4-100), email(@Email) |
| `FindUsernameRequest` | email(@Email) |
| `ResetPasswordRequest` | username, email, newPassword(@Size 4-100) |
| `AuthResponse` | token, username |

## Config
- `SecurityConfig`: OAuth2 + JWT. permitAll: `/api/auth/**`, `/oauth2/**`, `/login/oauth2/**`. Session: IF_REQUIRED
- `KakaoOAuth2UserService`: 카카오 userInfo → User DB upsert
- `OAuth2SuccessHandler`: JWT 발급 → `http://localhost:5173/?kakaoToken=...` 리다이렉트
- `GlobalExceptionHandler`: IllegalArgument→400, MethodArgumentNotValid→400
