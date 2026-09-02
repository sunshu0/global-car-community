package com.worldgarage.backend.controller;

import com.worldgarage.backend.dto.RegisterUserRequest;
import com.worldgarage.backend.dto.RegisterUserResponse;
import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.service.UserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserAccountService userAccountService;

  public AuthController(UserAccountService userAccountService) {
    this.userAccountService = userAccountService;
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterUserResponse> register(
      @RequestBody RegisterUserRequest request) {

    UserAccount registeredUser =
        userAccountService.register(
            request.email(), request.password(), request.displayName());

    RegisterUserResponse response =
        new RegisterUserResponse(
            registeredUser.getId(),
            registeredUser.getEmail(),
            registeredUser.getDisplayName(),
            registeredUser.getRole());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
