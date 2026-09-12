package com.worldgarage.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.repository.CarRepository;
import com.worldgarage.backend.repository.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CarDetailSessionBoundaryTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CarRepository carRepository;

  @Autowired
  private UserAccountRepository userAccountRepository;

  private Long carId;
  private Long ownerId;

  @AfterEach
  void removeTestData() {
    if (carId != null && carRepository.existsById(carId)) {
      carRepository.deleteById(carId);
    }
    if (ownerId != null && userAccountRepository.existsById(ownerId)) {
      userAccountRepository.deleteById(ownerId);
    }
  }

  @Test
  void approvedCarDetailLoadsOwnerOutsideRepositorySession() throws Exception {
    UserAccount owner =
        userAccountRepository.save(
            new UserAccount(
                "session-boundary-owner@example.com",
                "{bcrypt}test-password-hash",
                "Session Boundary Owner"));
    ownerId = owner.getId();

    Car car =
        new Car(
            "Nissan",
            "370Z",
            "Auckland, New Zealand");
    car.setOwner(owner);
    car.approve();
    car = carRepository.save(car);
    carId = car.getId();

    mockMvc.perform(get("/api/cars/{id}", carId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.owner.id").value(ownerId))
        .andExpect(
            jsonPath("$.owner.displayName")
                .value("Session Boundary Owner"));
  }
}
