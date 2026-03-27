package com.example.jwtcommon;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * JWT 공통 빈 자동 등록.
 * META-INF/spring/AutoConfiguration.imports 에 등록되어
 * 의존하는 서비스가 별도 설정 없이 JwtUtil·JwtSecurityConfigurer 빈을 사용한다.
 */
@AutoConfiguration
public class JwtAutoConfiguration {

    @Bean
    public JwtUtil jwtUtil(
            @Value("${jwt.secret:my-super-secret-key-for-jwt-token-demo-app-2024}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        return new JwtUtil(secret, expirationMs);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil) {
        return new JwtAuthenticationFilter(jwtUtil);
    }

    @Bean
    public JwtSecurityConfigurer jwtSecurityConfigurer(JwtAuthenticationFilter jwtAuthenticationFilter) {
        return new JwtSecurityConfigurer(jwtAuthenticationFilter);
    }
}
