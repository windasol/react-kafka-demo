# auth-service 상세

포트: 8084 | 패키지: `com.example.authservice`

## Entity
- `User`: id, username(unique), password(BCrypt), email, provider, name, createdAt
  - `create(username, encodedPassword, email)`
  - `createOAuth(username, email, provider, name)`
  - `changePassword(encodedPassword)`
  - `updateName(name)`

## Repository
- `UserRepository`
  - `findByUsername(String) → Optional<User>`
  - `existsByUsername(String) → boolean`
  - `findByEmail(String) → Optional<User>`
  - `findByUsernameAndEmail(String, String) → Optional<User>`

## Service
- `AuthService`
  - `register(RegisterRequest) → LoginResult`
  - `login(LoginRequest) → LoginResult`
  - `findUsername(FindUsernameRequest) → String`
  - `resetPassword(ResetPasswordRequest) → void`
  - `getProfile(username) → UserProfileResponse`
  - `changePassword(username, ChangePasswordRequest) → void`

## Controller `/api/auth`
- `POST /register` → AuthResponse (JWT HttpOnly 쿠키 발급)
- `POST /login` → AuthResponse (JWT HttpOnly 쿠키 발급)
- `POST /logout` → 쿠키 제거
- `POST /find-username` → `{username}`
- `POST /reset-password` → `{message}`
- `GET /profile` → UserProfileResponse (인증 필요)
- `PATCH /password` → 204 (인증 필요)

## DTO
| 클래스 | 필드 |
|--------|------|
| `LoginRequest` | username(@NotBlank), password(@NotBlank @Size≥4) |
| `RegisterRequest` | username(@Size 3-20), password(@Size 4-100), email(@Email) |
| `FindUsernameRequest` | email(@Email) |
| `ResetPasswordRequest` | username, email, newPassword(@Size 4-100) |
| `ChangePasswordRequest` | currentPassword, newPassword |
| `AuthResponse` | username |
| `UserProfileResponse` | username, email, name, provider |

## Config
- `SecurityConfig`: OAuth2 + JWT. permitAll: `/api/auth/**`, `/oauth2/**`, `/login/oauth2/**`. Session: IF_REQUIRED
- `KakaoOAuth2UserService`: 카카오 userInfo → User DB upsert
- `OAuth2SuccessHandler`: JWT 발급 → `http://localhost:5173/?kakaoToken=...` 리다이렉트
- `GlobalExceptionHandler`: IllegalArgument→400, MethodArgumentNotValid→400
