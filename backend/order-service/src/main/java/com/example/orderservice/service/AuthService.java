package com.example.orderservice.service;

import com.example.orderservice.config.JwtUtil;
import com.example.orderservice.dto.*;
import com.example.orderservice.entity.User;
import com.example.orderservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 인증 서비스 — 회원가입, 로그인, 카카오 로그인
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final KakaoOAuthService kakaoOAuthService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, KakaoOAuthService kakaoOAuthService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.kakaoOAuthService = kakaoOAuthService;
    }

    /** 회원가입 */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다: " + request.username());
        }
        User user = User.create(request.username(), passwordEncoder.encode(request.password()), request.email());
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername());
    }

    /** 로그인 */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername());
    }

    /** 아이디 찾기 */
    public String findUsername(FindUsernameRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 등록된 계정이 없습니다."));
        return user.getUsername();
    }

    /** 카카오 로그인 */
    @Transactional
    public AuthResponse kakaoLogin(KakaoLoginRequest request) {
        String accessToken = kakaoOAuthService.getAccessToken(request.code());
        Map<String, String> userInfo = kakaoOAuthService.getUserInfo(accessToken);

        String kakaoUsername = "kakao_" + userInfo.get("id");
        String nickname = userInfo.get("nickname");

        User user = userRepository.findByUsernameAndProvider(kakaoUsername, "KAKAO")
                .orElseGet(() -> {
                    User newUser = User.createOAuth(kakaoUsername, kakaoUsername + "@kakao.com", "KAKAO");
                    return userRepository.save(newUser);
                });

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token, nickname);
    }

    /** 비밀번호 재설정 */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByUsernameAndEmail(request.username(), request.email())
                .orElseThrow(() -> new IllegalArgumentException("사용자명과 이메일이 일치하는 계정이 없습니다."));
        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }
}
