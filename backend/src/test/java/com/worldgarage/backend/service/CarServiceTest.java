package com.worldgarage.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.repository.CarRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CarServiceTest {

  private CarRepository carRepository;
  private CarService carService;

  @BeforeEach
  void setup() {
    carRepository = mock(CarRepository.class);
    carService = new CarService(carRepository);
  }

  @Test
  void returnsAllCars() {
    List<Car> repositoryCars = List.of(
        new Car(1L, "Nissan", "370Z", "Auckland, New Zealand"),
        new Car(2L, "Toyota", "Supra", "Tokyo, Japan"),
        new Car(3L, "BMW", "M3", "Munich, Germany")
    );

    when(carRepository.findAll()).thenReturn(repositoryCars);

    List<Car> cars = carService.getAllCars();

    assertEquals(3, cars.size());
    assertEquals("Nissan", cars.get(0).getMake());
    assertEquals("370Z", cars.get(0).getModel());
  }

  @Test
  void returnsCarWhenIdExists() {
    Car repositoryCar = new Car(2L, "Toyota", "Supra", "Tokyo, Japan");

    when(carRepository.findById(2L))
        .thenReturn(Optional.of(repositoryCar));

    Car car = carService.getCarById(2L).orElseThrow();

    assertEquals("Toyota", car.getMake());
    assertEquals("Supra", car.getModel());
  }

  @Test
  void returnsEmptyWhenIdDoesNotExist() {
    when(carRepository.findById(99L))
        .thenReturn(Optional.empty());

    boolean resultIsEmpty = carService.getCarById(99L).isEmpty();

    assertTrue(resultIsEmpty);
  }

  @Test
  void createsCar() {
    Car savedCar = new Car(4L, "Mazda", "RX-7", "Auckland, New Zealand");

    when(carRepository.save(any(Car.class))).thenReturn(savedCar);

    Car createdCar = carService.createCar("Mazda", "RX-7", "Auckland, New Zealand");

    assertEquals(4L, createdCar.getId());
    assertEquals("Mazda", createdCar.getMake());
    assertEquals("RX-7", createdCar.getModel());

    verify(carRepository).save(any(Car.class));
  }
}
