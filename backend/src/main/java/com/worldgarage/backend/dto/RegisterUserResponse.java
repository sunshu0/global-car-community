package com.worldgarage.backend.dto;

import com.worldgarage.backend.model.UserRole;

public record RegisterUserResponse(
    Long id,
    String email,
    String displayName,
    UserRole role) {}
