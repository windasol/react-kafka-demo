package com.example.authservice.config;

import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 카카오 OAuth2 사용자 정보 로드 및 DB 저장
 */
@Service
public class KakaoOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public KakaoOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Long kakaoId = (Long) attributes.get("id");

        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        String email;
        if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
            email = (String) kakaoAccount.get("email");
        } else {
            email = "kakao_" + kakaoId + "@kakao.local";
        }

        String username = "kakao_" + kakaoId;

        @SuppressWarnings("unchecked")
        Map<String, Object> profile = kakaoAccount != null
                ? (Map<String, Object>) kakaoAccount.get("profile")
                : null;
        String name = (profile != null && profile.containsKey("nickname"))
                ? (String) profile.get("nickname")
                : username;

        userRepository.findByUsername(username)
                .ifPresentOrElse(
                        user -> user.updateName(name),
                        () -> userRepository.save(User.createOAuth(username, email, "KAKAO", name))
                );

        return oAuth2User;
    }
}
