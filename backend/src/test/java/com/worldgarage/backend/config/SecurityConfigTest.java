package com.worldgarage.backend.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void adminEndpointsRequireAuthentication() throws Exception {
    mockMvc.perform(get("/api/admin/cars")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "USER")
  void adminEndpointsRejectNonAdminUsers() throws Exception {
    mockMvc.perform(get("/api/admin/cars")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminUsersPassSecurityChecks() throws Exception {
    mockMvc.perform(get("/api/admin/cars")).andExpect(status().isNotFound());
  }

  @Test
  void passwordEncoderHashesAndMatchesPasswords() {
    String rawPassword = "Garage@2026";

    String firstHash = passwordEncoder.encode(rawPassword);
    String secondHash = passwordEncoder.encode(rawPassword);

    assertNotEquals(rawPassword, firstHash);
    assertNotEquals(firstHash, secondHash);

    assertTrue(passwordEncoder.matches(rawPassword, firstHash));
    assertTrue(passwordEncoder.matches(rawPassword, secondHash));
    assertFalse(passwordEncoder.matches("WrongPassword", firstHash));
  }
}
