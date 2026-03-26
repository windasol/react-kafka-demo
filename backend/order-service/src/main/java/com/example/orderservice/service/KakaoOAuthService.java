package com.example.orderservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 카카오 OAuth 서비스 — 인가코드로 액세스토큰 교환 및 사용자 정보 조회
 */
@Service
public class KakaoOAuthService {

    private final String clientId;
    private final String redirectUri;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public KakaoOAuthService(
            @Value("${kakao.client-id}") String clientId,
            @Value("${kakao.redirect-uri:http://localhost:5173}") String redirectUri) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /** 인가코드로 액세스토큰 교환 */
    public String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token", request, String.class);

        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            return json.get("access_token").asText();
        } catch (Exception e) {
            throw new IllegalArgumentException("카카오 액세스토큰 발급에 실패했습니다.");
        }
    }

    /** 액세스토큰으로 사용자 정보 조회 */
    public Map<String, String> getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, request, String.class);

        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            String kakaoId = json.get("id").asText();
            String nickname = json.path("kakao_account").path("profile").path("nickname").asText("카카오사용자");
            return Map.of("id", kakaoId, "nickname", nickname);
        } catch (Exception e) {
            throw new IllegalArgumentException("카카오 사용자 정보 조회에 실패했습니다.");
        }
    }
}
