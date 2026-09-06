package com.worldgarage.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.repository.CarRepository;
import com.worldgarage.backend.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PublicUserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserAccountRepository userAccountRepository;

  @Autowired
  private CarRepository carRepository;

  @Test
  void returnsPublicProfileWithOnlyOwnersApprovedCarsNewestFirst() throws Exception {
    UserAccount owner =
        userAccountRepository.save(
            new UserAccount(
                "profile-owner@example.com",
                "{bcrypt}secret-password-hash",
                "Profile Owner"));

    UserAccount otherOwner =
        userAccountRepository.save(
            new UserAccount(
                "profile-other@example.com",
                "{bcrypt}other-password-hash",
                "Other Owner"));

    Car olderApproved =
        new Car(
            "Nissan",
            "370Z",
            "Auckland, New Zealand",
            "https://example.com/370z.jpg");
    olderApproved.setOwner(owner);
    olderApproved.approve();
    olderApproved = carRepository.save(olderApproved);

    Car pending = new Car("Mazda", "RX-7", "Hiroshima, Japan");
    pending.setOwner(owner);
    carRepository.save(pending);

    Car otherOwnersCar = new Car("Toyota", "Supra", "Tokyo, Japan");
    otherOwnersCar.setOwner(otherOwner);
    otherOwnersCar.approve();
    carRepository.save(otherOwnersCar);

    Car newestApproved = new Car("Porsche", "911", "Paris, France");
    newestApproved.setOwner(owner);
    newestApproved.approve();
    newestApproved = carRepository.save(newestApproved);

    mockMvc.perform(get("/api/users/{id}", owner.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(owner.getId()))
        .andExpect(jsonPath("$.displayName").value("Profile Owner"))
        .andExpect(jsonPath("$.email").doesNotExist())
        .andExpect(jsonPath("$.passwordHash").doesNotExist())
        .andExpect(jsonPath("$.role").doesNotExist())
        .andExpect(jsonPath("$.cars.length()").value(2))
        .andExpect(jsonPath("$.cars[0].id").value(newestApproved.getId()))
        .andExpect(jsonPath("$.cars[0].model").value("911"))
        .andExpect(jsonPath("$.cars[1].id").value(olderApproved.getId()))
        .andExpect(jsonPath("$.cars[1].model").value("370Z"))
        .andExpect(jsonPath("$.cars[1].imageUrl").value("https://example.com/370z.jpg"));
  }

  @Test
  void returnsNotFoundWhenUserDoesNotExist() throws Exception {
    mockMvc.perform(get("/api/users/{id}", Long.MAX_VALUE))
        .andExpect(status().isNotFound());
  }
}
