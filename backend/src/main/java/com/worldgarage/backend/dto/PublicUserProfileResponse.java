package com.worldgarage.backend.dto;

import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.model.UserAccount;
import java.util.List;

public record PublicUserProfileResponse(
    Long id,
    String displayName,
    List<CarSummary> cars) {

  public static PublicUserProfileResponse from(
      UserAccount user,
      List<Car> approvedCars) {

    List<CarSummary> carSummaries =
        approvedCars.stream()
            .map(CarSummary::from)
            .toList();

    return new PublicUserProfileResponse(
        user.getId(),
        user.getDisplayName(),
        carSummaries);
  }

  public record CarSummary(
      Long id,
      String make,
      String model,
      String location,
      String imageUrl) {

    public static CarSummary from(Car car) {
      return new CarSummary(
          car.getId(),
          car.getMake(),
          car.getModel(),
          car.getLocation(),
          car.getImageUrl());
    }
  }
}
