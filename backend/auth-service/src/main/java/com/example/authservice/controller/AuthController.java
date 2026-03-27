package com.example.authservice.controller;

import com.example.authservice.dto.*;
import com.example.authservice.service.AuthService;
import com.example.authservice.service.AuthService.LoginResult;
import com.example.authservice.support.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        CookieUtils.setAuthCookies(response, result.token(), result.username());
        return ResponseEntity.ok(new AuthResponse(result.username()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletResponse response) {
        LoginResult result = authService.login(request);
        CookieUtils.setAuthCookies(response, result.token(), result.username());
        return ResponseEntity.ok(new AuthResponse(result.username()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        CookieUtils.clearAuthCookies(response);
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
}
