package com.worldgarage.backend.dto;

import java.util.Map;

public record ValidationErrorResponse(Map<String, String> errors) {}
