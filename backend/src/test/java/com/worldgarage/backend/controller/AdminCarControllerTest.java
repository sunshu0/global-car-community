package com.worldgarage.backend.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminCarControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private CarRepository carRepository;

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminApprovesPendingCar() throws Exception {
    Car pendingCar = carRepository.save(new Car("Nissan", "370Z", "Auckland, New Zealand"));

    mockMvc
        .perform(patch("/api/admin/cars/{id}/approve", pendingCar.getId()).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(pendingCar.getId()))
        .andExpect(jsonPath("$.reviewStatus").value("APPROVED"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminRejectsPendingCar() throws Exception {
    Car pendingCar =
        carRepository.save(
            new Car(
                "Mazda",
                "RX-8",
                "Hiroshima, Japan"));

    mockMvc
        .perform(
            patch(
                "/api/admin/cars/{id}/reject",
                pendingCar.getId())
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(pendingCar.getId()))
        .andExpect(jsonPath("$.reviewStatus").value("REJECTED"));

    mockMvc
        .perform(get("/api/cars/{id}", pendingCar.getId()))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void returnsOnlyPendingCars() throws Exception {
    Car pendingCar =
        carRepository.save(
            new Car(
                "Mazda",
                "Pending RX-7",
                "Hiroshima, Japan"));

    Car approvedCar =
        new Car(
            "Nissan",
            "Approved 370Z",
            "Auckland, New Zealand");

    approvedCar.approve();
    carRepository.save(approvedCar);

    mockMvc.perform(get("/api/admin/cars"))
        .andExpect(status().isOk())
        .andExpect(
          jsonPath("$[?(@.id == " + pendingCar.getId() + ")]")
              .exists())
        .andExpect(
            jsonPath("$[?(@.model == 'Approved 370Z')]")
                .doesNotExist());
  }
}
