package com.example.authservice.config;

import com.example.jwtcommon.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 카카오 OAuth2 인증 성공 시 JWT 발급 후 프론트엔드로 리다이렉트
 */
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    public OAuth2SuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Long kakaoId = (Long) attributes.get("id");
        String username = "kakao_" + kakaoId;

        String token = jwtUtil.generateToken(username);
        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);

        String redirectUrl = "http://localhost:5173/?kakaoToken=" + token + "&kakaoUsername=" + encodedUsername;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
