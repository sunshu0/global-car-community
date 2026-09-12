package com.worldgarage.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCarRequest(
    @NotBlank(message = "Make is required.")
    @Size(max = 100, message = "Make must be at most 100 characters.")
    String make,
    @NotBlank(message = "Model is required.")
    @Size(max = 100, message = "Model must be at most 100 characters.")
    String model,
    @NotBlank(message = "Location is required.")
    @Size(max = 255, message = "Location must be at most 255 characters.")
    String location,
    @Size(max = 2048, message = "Image URL must be at most 2048 characters.")
    String imageUrl) {}
