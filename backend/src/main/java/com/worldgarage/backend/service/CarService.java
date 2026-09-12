package com.worldgarage.backend.service;

import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.model.ReviewStatus;
import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.repository.CarRepository;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CarService {

  private final CarRepository carRepository;

  public CarService(CarRepository carRepository) {
    this.carRepository = carRepository;
  }

  public List<Car> getAllCars() {
    return carRepository.findAllByReviewStatus(ReviewStatus.APPROVED);
  }

  public List<Car> getPendingCars() {
    return carRepository.findAllByReviewStatus(ReviewStatus.PENDING);
  }

  public Optional<Car> getCarById(long id) {
    return carRepository.findByIdAndReviewStatus(id, ReviewStatus.APPROVED);
  }

  public List<Car> getApprovedCarsByOwnerId(long ownerId) {
    return carRepository
        .findAllByOwnerIdAndReviewStatusOrderByIdDesc(
            ownerId,
            ReviewStatus.APPROVED);
  }

  public List<Car> getCarsByOwnerEmail(String email) {
    String normalizedEmail =
        email.toLowerCase(Locale.ROOT).trim();

    return carRepository.findAllByOwnerEmailOrderByIdDesc(normalizedEmail);
  }

  public Car createCar(String make, String model, String location) {
    return createCar(make, model, location, null);
  }

  public Car createCar(
      String make,
      String model,
      String location,
      String imageUrl
  ) {
    return createCar(make, model, location, imageUrl, null);
  }

  public Car createCar(
      String make,
      String model,
      String location,
      String imageUrl,
      UserAccount owner
  ) {
    Car car = new Car(make, model, location, imageUrl);
    car.setOwner(owner);
    return carRepository.save(car);
  }

  public Optional<Car> approveCar(long id) {
    return carRepository.findById(id)
        .map(car -> {
          car.approve();
          return carRepository.save(car);
        });
  }

  public Optional<Car> rejectCar(long id) {
    return carRepository.findById(id)
        .map(car -> {
          car.reject();
          return carRepository.save(car);
        });
  }
}
