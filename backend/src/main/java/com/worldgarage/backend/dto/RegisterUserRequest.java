package com.worldgarage.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid.")
    @Size(max = 320, message = "Email must not exceed 320 characters.")
    String email,
    @NotBlank(message = "Password is required.")
    @Size(
        min = 8,
        max = 72,
        message = "Password must be between 8 and 72 characters.")
    String password,
    @NotBlank(message = "Display name is required")
    @Size(max = 50, message = "Display name must be at most 50 characters.")
    String displayName) {}
