package com.worldgarage.backend.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private UserAccountRepository userAccountRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void registersUserWithoutAuthentication() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "Owner@Example.com",
                      "password": "Garage@2026",
                      "displayName": "Garage Owner"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.email").value("owner@example.com"))
        .andExpect(jsonPath("$.displayName").value("Garage Owner"))
        .andExpect(jsonPath("$.role").value("USER"))
        .andExpect(jsonPath("$.password").doesNotExist())
        .andExpect(jsonPath("$.passwordHash").doesNotExist());

    UserAccount savedUser =
        userAccountRepository.findByEmail("owner@example.com").orElseThrow();

    assertTrue(
        passwordEncoder.matches("Garage@2026", savedUser.getPasswordHash()));
  }

  @Test
  void returnsConflictWhenEmailAlreadyExists() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "duplicate@example.com",
                      "password": "Garage@2026",
                      "displayName": "First Owner"
                    }
                    """))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "Duplicate@Example.com",
                      "password": "AnotherPassword@2026",
                      "displayName": "Second Owner"
                    }
                    """))
        .andExpect(status().isConflict())
        .andExpect(
            jsonPath("$.message")
                .value("Email already exists: duplicate@example.com"));
  }

  @Test
  void returnsBadRequestWhenRegistrationIsInvalid() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "invalid-email",
                      "password": "short",
                      "displayName": ""
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.errors.email")
                .value("Email must be valid."))
        .andExpect(
            jsonPath("$.errors.password")
                .value("Password must be between 8 and 72 characters."))
        .andExpect(
            jsonPath("$.errors.displayName")
                .value("Display name is required"));

    assertFalse(userAccountRepository.existsByEmail("invalid-email"));
  }

  @Test void logInAndSavesAuthenticationInSession() throws Exception {
    //Arrange
    String email = "login-owner@example.com";
    String passwordHash = passwordEncoder.encode("Garage@2026");

    userAccountRepository.save(new UserAccount(email,passwordHash,"Garage Owner"));

    //Act
    MvcResult result =
    mockMvc.perform(post("/api/auth/login")
    .with(csrf())
    .contentType(MediaType.APPLICATION_JSON)
    .content(
        """
                {
        "email":"login-owner@example.com",
        "password":"Garage@2026"
                }
                """))
                .andExpect(status().isOk())
                .andReturn();

                //Assert
                MockHttpSession session =
                (MockHttpSession) result.getRequest().getSession(false);

                assertNotNull(session);

                SecurityContext context =
      (SecurityContext)
          session.getAttribute(
              HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

  assertNotNull(context);
  assertNotNull(context.getAuthentication());
  assertTrue(context.getAuthentication().isAuthenticated());
  assertEquals(email, context.getAuthentication().getName());
  }

  @Test
  void returnsUnauthorizedWhenPasswordIsWrong() throws Exception {
    // Arrange
    String passwordHash = passwordEncoder.encode("Garage@2026");

    userAccountRepository.save(
        new UserAccount(
            "wrong-password@example.com",
            passwordHash,
            "Garage Owner"));

    // Act + Assert
    mockMvc
        .perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "wrong-password@example.com",
                      "password": "WrongPassword@2026"
                    }
                    """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void returnsUnauthorizedWhenEmailDoesNotExist() throws Exception {
    // Arrange
    assertFalse(userAccountRepository.existsByEmail("missing-login@example.com"));

    // Act + Assert
    mockMvc
        .perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "missing-login@example.com",
                      "password": "Garage@2026"
                    }
                    """))
        .andExpect(status().isUnauthorized());
  }
}
