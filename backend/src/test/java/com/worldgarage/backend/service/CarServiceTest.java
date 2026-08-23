package com.worldgarage.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.worldgarage.backend.model.Car;
import java.util.List;
import org.junit.jupiter.api.Test;

class CarServiceTest {

  @Test
  void returnAllCars() {
    CarService carService = new CarService();

    List<Car> cars = carService.getAllCars();

    assertEquals(3, cars.size());
    assertEquals("Nissan", cars.get(0).make());
    assertEquals("370Z", cars.get(0).model());
  }
}
