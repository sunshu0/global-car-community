package com.worldgarage.backend.dto;

public record RegisterUserRequest(
    String email,
    String password,
    String displayName) {}
