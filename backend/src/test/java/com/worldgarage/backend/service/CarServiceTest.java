package com.worldgarage.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.worldgarage.backend.model.Car;
import java.util.List;
import org.junit.jupiter.api.Test;

class CarServiceTest {

  @Test
  void returnsAllCars() {
    CarService carService = new CarService();

    List<Car> cars = carService.getAllCars();

    assertEquals(3, cars.size());
    assertEquals("Nissan", cars.get(0).getMake());
    assertEquals("370Z", cars.get(0).getModel());
  }

  @Test
  void returnsCarWhenIdExists() {
    CarService carService = new CarService();

    Car car = carService.getCarById(2L).orElseThrow();

    assertEquals("Toyota", car.getMake());
    assertEquals("Supra", car.getModel());
  }

  @Test
  void returnsEmptyWhenIdDoesNotExist() {
    CarService carService = new CarService();

    boolean resultIsEmpty = carService.getCarById(99L).isEmpty();

    assertTrue(resultIsEmpty);
  }
}
