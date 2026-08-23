package com.worldgarage.backend.model;

public record Car(
    long id,
    String make,
    String model,
    String location
) {
}
