package com.example.authservice.support;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

/**
 * 인증 쿠키 생성/삭제 유틸리티
 */
public final class CookieUtils {

    private CookieUtils() {}

    public static void setAuthCookies(HttpServletResponse response, String token, String username) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildTokenCookie(token).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildUsernameCookie(username).toString());
    }

    public static void clearAuthCookies(HttpServletResponse response) {
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
