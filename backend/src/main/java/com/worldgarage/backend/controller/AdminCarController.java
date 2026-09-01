package com.worldgarage.backend.controller;

import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.service.CarService;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/cars")
public class AdminCarController {

  private final CarService carService;

  public AdminCarController(CarService carService) {
    this.carService = carService;
  }

  @PatchMapping("/{id}/approve")
  public ResponseEntity<Car> approveCar(@PathVariable long id) {
    Optional<Car> approvedCar = carService.approveCar(id);

    if (approvedCar.isPresent()) {
      return ResponseEntity.ok(approvedCar.get());
    }

    return ResponseEntity.notFound().build();
  }
}
