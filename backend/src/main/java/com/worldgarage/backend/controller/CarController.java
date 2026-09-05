package com.worldgarage.backend.controller;

import com.worldgarage.backend.dto.CreateCarRequest;
import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.service.CarService;
import com.worldgarage.backend.service.UserAccountService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cars")
public class CarController {

  private final CarService carService;
  private final UserAccountService userAccountService;

  public CarController(
      CarService carService,
      UserAccountService userAccountService) {
    this.carService = carService;
    this.userAccountService = userAccountService;
  }

  @GetMapping
  public List<Car> getCars() {
    return carService.getAllCars();
  }

  @PostMapping
  public ResponseEntity<Car> createCar(
      @RequestBody CreateCarRequest request,
      Authentication authentication) {
    UserAccount owner =
        userAccountService
            .getUserByEmail(authentication.getName())
            .orElseThrow();

    Car createdCar =
        carService.createCar(
            request.make(),
            request.model(),
            request.location(),
            request.imageUrl(),
            owner);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createdCar);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Car> getCarById(@PathVariable long id) {
    Optional<Car> car = carService.getCarById(id);

    if (car.isPresent()) {
      return ResponseEntity.ok(car.get());
    }

    return ResponseEntity.notFound().build();
  }
}
