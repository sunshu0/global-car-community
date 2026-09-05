package com.worldgarage.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.model.ReviewStatus;
import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.repository.CarRepository;
import com.worldgarage.backend.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CarControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CarRepository carRepository;

  @Autowired
  private UserAccountRepository userAccountRepository;

  @Test
  @WithMockUser(username = "owner@example.com", roles = "USER")
  void createsPendingCarThatIsNotPubliclyVisible() throws Exception {
    UserAccount owner =
        userAccountRepository.save(
            new UserAccount(
                "owner@example.com",
                "{bcrypt}test-password-hash",
                "Garage Owner"));

    long carCountBefore = carRepository.count();

    mockMvc.perform(post("/api/cars")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "make": "Mazda",
                  "model": "RX-7",
                  "location": "Auckland, New Zealand",
                  "imageUrl": "https://example.com/rx7.jpg"
                }
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.make").value("Mazda"))
        .andExpect(jsonPath("$.model").value("RX-7"))
        .andExpect(jsonPath("$.location").value("Auckland, New Zealand"))
        .andExpect(jsonPath("$.imageUrl").value("https://example.com/rx7.jpg"))
        .andExpect(jsonPath("$.reviewStatus").value("PENDING"))
        .andExpect(jsonPath("$.owner").doesNotExist());
    assertEquals(carCountBefore + 1, carRepository.count());

    Car pendingCar = carRepository
        .findAllByReviewStatus(ReviewStatus.PENDING)
        .stream()
        .filter(car -> car.getMake().equals("Mazda"))
        .findFirst()
        .orElseThrow();

    assertEquals(owner.getId(), pendingCar.getOwner().getId());
    assertEquals("owner@example.com", pendingCar.getOwner().getEmail());

    mockMvc.perform(get("/api/cars/{id}", pendingCar.getId()))
        .andExpect(status().isNotFound());
  }

  @Test
  void rejectsAnonymousCarSubmission() throws Exception {
    mockMvc.perform(post("/api/cars")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "make": "Mazda",
                  "model": "RX-7",
                  "location": "Auckland, New Zealand",
                  "imageUrl": null
                }
                """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void returnsCarWhenIdExists() throws Exception {
    mockMvc.perform(get("/api/cars/2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(2))
        .andExpect(jsonPath("$.make").value("Toyota"))
        .andExpect(jsonPath("$.model").value("Supra"));
  }

  @Test
  void returnsSafeOwnerSummaryForApprovedCar() throws Exception {
    UserAccount owner =
        userAccountRepository.save(
            new UserAccount(
                "public-owner@example.com",
                "{bcrypt}secret-password-hash",
                "Public Owner"));

    Car car =
        new Car(
            "Nissan",
            "370Z",
            "Auckland, New Zealand");

    car.setOwner(owner);
    car.approve();
    car = carRepository.save(car);

    mockMvc.perform(get("/api/cars/{id}", car.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.owner.id").value(owner.getId()))
        .andExpect(jsonPath("$.owner.displayName").value("Public Owner"))
        .andExpect(jsonPath("$.owner.email").doesNotExist())
        .andExpect(jsonPath("$.owner.passwordHash").doesNotExist())
        .andExpect(jsonPath("$.owner.role").doesNotExist());
  }

  @Test
  void returnsNotFoundWhenIdDoesNotExist() throws Exception {
    mockMvc.perform(get("/api/cars/99"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "owner@example.com", roles = "USER")
  void returnsOnlyCurrentUsersCarsNewestFirst() throws Exception {
    UserAccount owner =
        userAccountRepository.save(
            new UserAccount(
                "owner@example.com",
                "{bcrypt}owner-password-hash",
                "Garage Owner"));

    UserAccount otherOwner =
        userAccountRepository.save(
            new UserAccount(
                "other@example.com",
                "{bcrypt}other-password-hash",
                "Other Owner"));

    Car olderOwnerCar =
        new Car("Nissan", "370Z", "Auckland, New Zealand");
    olderOwnerCar.setOwner(owner);
    olderOwnerCar.approve();
    olderOwnerCar = carRepository.save(olderOwnerCar);

    Car otherCar =
        new Car("Toyota", "Supra", "Tokyo, Japan");
    otherCar.setOwner(otherOwner);
    carRepository.save(otherCar);

    Car newestOwnerCar =
        new Car("Mazda", "RX-7", "Hiroshima, Japan");
    newestOwnerCar.setOwner(owner);
    newestOwnerCar = carRepository.save(newestOwnerCar);

    mockMvc.perform(get("/api/cars/mine"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(newestOwnerCar.getId()))
        .andExpect(jsonPath("$[0].model").value("RX-7"))
        .andExpect(jsonPath("$[0].reviewStatus").value("PENDING"))
        .andExpect(jsonPath("$[0].owner").doesNotExist())
        .andExpect(jsonPath("$[1].id").value(olderOwnerCar.getId()))
        .andExpect(jsonPath("$[1].model").value("370Z"))
        .andExpect(jsonPath("$[1].reviewStatus").value("APPROVED"))
        .andExpect(jsonPath("$[1].owner").doesNotExist());
  }

  @Test
  void rejectsAnonymousMyCarsRequest() throws Exception {
    mockMvc.perform(get("/api/cars/mine"))
        .andExpect(status().isUnauthorized());
  }
}
