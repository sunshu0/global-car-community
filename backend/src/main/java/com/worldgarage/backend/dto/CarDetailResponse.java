package com.worldgarage.backend.dto;

import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.model.UserAccount;

public record CarDetailResponse(
    Long id,
    String make,
    String model,
    String location,
    String imageUrl,
    OwnerSummary owner) {

  public static CarDetailResponse from(Car car) {
    UserAccount owner = car.getOwner();

    OwnerSummary ownerSummary =
        owner == null
            ? null
            : new OwnerSummary(owner.getId(), owner.getDisplayName());

    return new CarDetailResponse(
        car.getId(),
        car.getMake(),
        car.getModel(),
        car.getLocation(),
        car.getImageUrl(),
        ownerSummary);
  }

  public record OwnerSummary(
      Long id,
      String displayName) {}
}
