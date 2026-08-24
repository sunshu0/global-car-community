package com.worldgarage.backend.service;

import com.worldgarage.backend.model.Car;
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
    return carRepository.findAll();
  }

  public Optional<Car> getCarById(long id) {
    return carRepository.findById(id);
  }

  public Car createCar(String make, String model, String location) {
    Car car = new Car(make, model, location);
    return carRepository.save(car);
  }
}
