package com.worldgarage.backend.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private UserAccountRepository userAccountRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void registersUserWithoutAuthentication() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/register")
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
}
