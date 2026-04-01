package com.example.authservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 사용자 엔티티
 */
@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column
    private String provider;

    @Column
    private String name;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected User() {}

    public static User create(String username, String encodedPassword, String email) {
        User user = new User();
        user.username = username;
        user.password = encodedPassword;
        user.email = email;
        user.createdAt = LocalDateTime.now();
        return user;
    }

    public static User createOAuth(String username, String email, String provider, String name) {
        User user = new User();
        user.username = username;
        user.password = "";
        user.email = email;
        user.provider = provider;
        user.name = name;
        user.createdAt = LocalDateTime.now();
        return user;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
