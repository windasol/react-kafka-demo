package com.example.authservice.service;

import com.example.authservice.dto.*;
import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.jwtcommon.JwtUtil;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스 — 회원가입, 로그인
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /** 토큰 + 사용자명을 함께 반환하는 내부 결과 */
    public record LoginResult(String token, String username) {}

    @Transactional
    public LoginResult register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다: " + request.username());
        }
        User user = User.create(request.username(), passwordEncoder.encode(request.password()), request.email());
        userRepository.save(user);
        return new LoginResult(jwtUtil.generateToken(user.getUsername()), user.getUsername());
    }

    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return new LoginResult(jwtUtil.generateToken(user.getUsername()), user.getUsername());
    }

    public String findUsername(FindUsernameRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 등록된 계정이 없습니다."));
        return user.getUsername();
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByUsernameAndEmail(request.username(), request.email())
                .orElseThrow(() -> new IllegalArgumentException("사용자명과 이메일이 일치하는 계정이 없습니다."));
        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return new UserProfileResponse(user.getUsername(), user.getEmail());
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }
}
