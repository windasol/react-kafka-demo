package com.example.authservice.dto;

/** 인증 성공 응답 — 토큰은 HttpOnly 쿠키로 전달, 본문에는 username만 포함 */
public record AuthResponse(
        String username
) {}
