package com.worldgarage.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.model.ReviewStatus;
import com.worldgarage.backend.repository.CarRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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

    when(carRepository.findAllByReviewStatus(ReviewStatus.APPROVED)).thenReturn(repositoryCars);

    List<Car> cars = carService.getAllCars();

    assertEquals(3, cars.size());
    assertEquals("Nissan", cars.get(0).getMake());
    assertEquals("370Z", cars.get(0).getModel());
    verify(carRepository)
        .findAllByReviewStatus(ReviewStatus.APPROVED);
  }

  @Test
  void returnsCarWhenIdExists() {
    Car repositoryCar = new Car(2L, "Toyota", "Supra", "Tokyo, Japan");

    when(carRepository.findByIdAndReviewStatus(2L, ReviewStatus.APPROVED))
        .thenReturn(Optional.of(repositoryCar));

    Car car = carService.getCarById(2L).orElseThrow();

    assertEquals("Toyota", car.getMake());
    assertEquals("Supra", car.getModel());
    verify(carRepository).findByIdAndReviewStatus(
        2L,
        ReviewStatus.APPROVED
    );
  }

  @Test
  void returnsEmptyWhenIdDoesNotExist() {
    when(carRepository.findByIdAndReviewStatus(99L, ReviewStatus.APPROVED))
        .thenReturn(Optional.empty());

    boolean resultIsEmpty = carService.getCarById(99L).isEmpty();

    assertTrue(resultIsEmpty);
    verify(carRepository).findByIdAndReviewStatus(
        99L,
        ReviewStatus.APPROVED
    );
  }

  @Test
  void createsCar() {
    String imageUrl = "https://example.com/rx7.jpg";
    Car savedCar = new Car(
        4L,
        "Mazda",
        "RX-7",
        "Auckland, New Zealand",
        imageUrl
    );

    when(carRepository.save(any(Car.class))).thenReturn(savedCar);

    Car createdCar = carService.createCar(
        "Mazda",
        "RX-7",
        "Auckland, New Zealand",
        imageUrl
    );

    assertEquals(4L, createdCar.getId());
    assertEquals("Mazda", createdCar.getMake());
    assertEquals("RX-7", createdCar.getModel());
    assertEquals(imageUrl, createdCar.getImageUrl());

    ArgumentCaptor<Car> carCaptor = ArgumentCaptor.forClass(Car.class);
    verify(carRepository).save(carCaptor.capture());

    Car carPassedToRepository = carCaptor.getValue();
    assertEquals(imageUrl, carPassedToRepository.getImageUrl());
    assertEquals(ReviewStatus.PENDING, carPassedToRepository.getReviewStatus());
  }
}
