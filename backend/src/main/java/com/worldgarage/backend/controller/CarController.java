package com.worldgarage.backend.controller;

import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.service.CarService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cars")
public class CarController {

  private final CarService carService;

  public CarController(CarService carService) {
    this.carService = carService;
  }

  @GetMapping
  public List<Car> getCars() {
    return carService.getAllCars();
  }
}
