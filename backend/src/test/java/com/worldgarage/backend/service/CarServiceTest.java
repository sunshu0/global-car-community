package com.worldgarage.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.model.ReviewStatus;
import com.worldgarage.backend.model.UserAccount;
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

  @Test
  void approvesCarWhenIdExists() {
    Car pendingCar = new Car(
        4L,
        "Mazda",
        "RX-7",
        "Hiroshima, Japan"
    );

    when(carRepository.findById(4L))
        .thenReturn(Optional.of(pendingCar));
    when(carRepository.save(pendingCar))
        .thenReturn(pendingCar);

    Car approvedCar = carService.approveCar(4L).orElseThrow();

    assertEquals(ReviewStatus.APPROVED, approvedCar.getReviewStatus());
    verify(carRepository).findById(4L);
    verify(carRepository).save(pendingCar);
  }

  @Test
  void returnsEmptyWhenApprovingMissingCar() {
    when(carRepository.findById(99L))
        .thenReturn(Optional.empty());

    Optional<Car> result = carService.approveCar(99L);

    assertTrue(result.isEmpty());
    verify(carRepository).findById(99L);
    verify(carRepository, never()).save(any(Car.class));
  }

  @Test
  void createsCarWithOwner() {
    // Arrange
    UserAccount owner =
        new UserAccount(
            "owner@example.com",
            "{bcrypt}password-hash",
            "Garage Owner");

    when(carRepository.save(any(Car.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Car createdCar =
        carService.createCar(
            "Nissan",
            "370Z",
            "Auckland, New Zealand",
            "https://example.com/370z.jpg",
            owner);

    // Assert
    assertSame(owner, createdCar.getOwner());

    ArgumentCaptor<Car> carCaptor =
        ArgumentCaptor.forClass(Car.class);

    verify(carRepository).save(carCaptor.capture());

    Car carPassedToRepository = carCaptor.getValue();
    assertSame(owner, carPassedToRepository.getOwner());
  }
}
