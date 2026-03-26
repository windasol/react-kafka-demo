package com.example.orderservice.entity;

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

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
