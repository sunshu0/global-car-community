package com.worldgarage.backend.service;

import com.worldgarage.backend.model.Car;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CarService {

  public List<Car> getAllCars() {
    return List.of(
        new Car(1L, "Nissan", "370Z", "Auckland, New Zealand"),
        new Car(2L, "Toyota", "Supra", "Tokyo, Japan"),
        new Car(3L, "BMW", "M3", "Munich, Germany")
    );
  }

  public Optional<Car> getCarById(long id) {
    for (Car car : getAllCars()) {
      if (car.id() == id) {
        return Optional.of(car);
      }
    }

    return Optional.empty();
  }
}
