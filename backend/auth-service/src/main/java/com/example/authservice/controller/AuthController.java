package com.example.authservice.controller;

import com.example.authservice.dto.*;
import com.example.authservice.service.AuthService;
import com.example.authservice.service.AuthService.LoginResult;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

/**
 * 인증 컨트롤러 — JWT를 HttpOnly 쿠키로 발급
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletResponse response) {
        LoginResult result = authService.register(request);
        setAuthCookies(response, result.token(), result.username());
        return ResponseEntity.ok(new AuthResponse(result.username()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletResponse response) {
        LoginResult result = authService.login(request);
        setAuthCookies(response, result.token(), result.username());
        return ResponseEntity.ok(new AuthResponse(result.username()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        clearAuthCookies(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/find-username")
    public ResponseEntity<Map<String, String>> findUsername(@Valid @RequestBody FindUsernameRequest request) {
        return ResponseEntity.ok(Map.of("username", authService.findUsername(request)));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }

    /** access_token(HttpOnly) + username(JS 읽기 가능) 쿠키 설정 */
    static void setAuthCookies(HttpServletResponse response, String token, String username) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildTokenCookie(token).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildUsernameCookie(username).toString());
    }

    static void clearAuthCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                ResponseCookie.from("access_token", "").httpOnly(true).path("/").maxAge(0).sameSite("Lax").build().toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                ResponseCookie.from("username", "").path("/").maxAge(0).sameSite("Lax").build().toString());
    }

    private static ResponseCookie buildTokenCookie(String token) {
        return ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(false)   // 운영: true (HTTPS)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("Lax")
                .build();
    }

    private static ResponseCookie buildUsernameCookie(String username) {
        return ResponseCookie.from("username", username)
                .httpOnly(false)  // JS에서 읽어 로그인 상태 표시용
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("Lax")
                .build();
    }
}
