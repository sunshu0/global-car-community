package com.worldgarage.backend.controller;

import com.worldgarage.backend.dto.PublicUserProfileResponse;
import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.service.CarService;
import com.worldgarage.backend.service.UserAccountService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class PublicUserController {

  private final UserAccountService userAccountService;
  private final CarService carService;

  public PublicUserController(
      UserAccountService userAccountService,
      CarService carService) {
    this.userAccountService = userAccountService;
    this.carService = carService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<PublicUserProfileResponse> getProfile(
      @PathVariable long id) {

    Optional<UserAccount> user =
        userAccountService.getUserById(id);

    if (user.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    List<Car> approvedCars =
        carService.getApprovedCarsByOwnerId(id);

    PublicUserProfileResponse response =
        PublicUserProfileResponse.from(
            user.get(),
            approvedCars);

    return ResponseEntity.ok(response);
  }
}
