package com.worldgarage.backend.controller;

import com.worldgarage.backend.dto.RegisterUserRequest;
import com.worldgarage.backend.dto.RegisterUserResponse;
import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import com.worldgarage.backend.dto.LoginRequest;
import com.worldgarage.backend.dto.CurrentUserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserAccountService userAccountService;
  private final AuthenticationManager authenticationManager;
  private final SecurityContextRepository securityContextRepository;
  private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

  public AuthController(
      UserAccountService userAccountService,
      AuthenticationManager authenticationManager,
      SecurityContextRepository securityContextRepository,
      SessionAuthenticationStrategy sessionAuthenticationStrategy) {
    this.userAccountService = userAccountService;
    this.authenticationManager = authenticationManager;
    this.securityContextRepository = securityContextRepository;
    this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterUserResponse> register(
      @Valid @RequestBody RegisterUserRequest request) {

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

  @GetMapping("/csrf")
  public CsrfToken csrf(CsrfToken token) {
    return token;
  }

  @GetMapping("/me")
  public ResponseEntity<CurrentUserResponse> me(Authentication authentication) {
    return userAccountService.getUserByEmail(authentication.getName())
        .map(user -> ResponseEntity.ok(new CurrentUserResponse(
            user.getId(), user.getEmail(), user.getDisplayName(), user.getRole())))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
  }

  @PostMapping("/login")
  public ResponseEntity<Void> login(
      @Valid @RequestBody LoginRequest loginRequest,
      HttpServletRequest request,
      HttpServletResponse response) {

    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.email(), loginRequest.password()));

    sessionAuthenticationStrategy.onAuthentication(authentication, request, response);

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);

    securityContextRepository.saveContext(context, request, response);

    return ResponseEntity.ok().build();
  }
}
