package com.worldgarage.backend.service;

import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.model.ReviewStatus;
import com.worldgarage.backend.repository.CarRepository;
import java.util.List;
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

  public Optional<Car> getCarById(long id) {
    return carRepository.findByIdAndReviewStatus(id, ReviewStatus.APPROVED);
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
    Car car = new Car(make, model, location, imageUrl);
    return carRepository.save(car);
  }
}
