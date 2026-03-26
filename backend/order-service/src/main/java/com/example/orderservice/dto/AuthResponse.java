package com.example.orderservice.dto;

public record AuthResponse(
        String token,
        String username
) {}
