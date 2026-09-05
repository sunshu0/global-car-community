package com.worldgarage.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SessionFlowTest {
  @Autowired private MockMvc mockMvc;

  private record Token(MockHttpSession session, String header, String value) {}

  // Read the actual HTTP token, without the csrf() test helper.
  private Token token(MockHttpSession session) throws Exception {
    var request = get("/api/auth/csrf");
    if (session != null) request.session(session);
    MvcResult result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
    String body = result.getResponse().getContentAsString();
    return new Token((MockHttpSession) result.getRequest().getSession(false),
        JsonPath.read(body, "$.headerName"), JsonPath.read(body, "$.token"));
  }

  @Test
  void registersLogsInRestoresIdentityAndLogsOutWithRealCsrf() throws Exception {
    Token before = token(null);
    assertNotNull(before.session());
    mockMvc.perform(post("/api/auth/register").session(before.session())
        .header(before.header(), before.value()).contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"email":"session@example.com","password":"Garage@2026","displayName":"Session Owner"}
            """)).andExpect(status().isCreated());

    String oldSessionId = before.session().getId();
    mockMvc.perform(post("/api/auth/login").session(before.session())
        .header(before.header(), before.value()).contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"email":"session@example.com","password":"Garage@2026"}
            """)).andExpect(status().isOk());
    assertNotEquals(oldSessionId, before.session().getId());

    mockMvc.perform(get("/api/auth/me").session(before.session()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("session@example.com"))
        .andExpect(jsonPath("$.displayName").value("Session Owner"))
        .andExpect(jsonPath("$.role").value("USER"))
        .andExpect(jsonPath("$.passwordHash").doesNotExist())
        .andExpect(jsonPath("$.password").doesNotExist());

    // Login rotates the CSRF secret. A pre-login token must not log us out.
    mockMvc.perform(post("/api/auth/logout").session(before.session())
        .header(before.header(), before.value())).andExpect(status().isForbidden());
    Token after = token(before.session());
    mockMvc.perform(post("/api/auth/logout").session(after.session())
        .header(after.header(), after.value())).andExpect(status().isNoContent());
    assertTrue(after.session().isInvalid());
    mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void anonymousUsersCannotReadCurrentUser() throws Exception {
    mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized())
        .andExpect(header().doesNotExist("WWW-Authenticate"));
  }

  @Test
  void authWritesRejectMissingCsrf() throws Exception {
    for (String path : new String[] {"/api/auth/register", "/api/auth/login", "/api/auth/logout"}) {
      mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{}"))
          .andExpect(status().isForbidden());
    }
  }

  @Test
  void invalidLoginInputReturnsBadRequestWithoutAuthenticating() throws Exception {
    Token csrf = token(null);
    mockMvc.perform(post("/api/auth/login").session(csrf.session())
        .header(csrf.header(), csrf.value()).contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"email":"invalid","password":""}
            """)).andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/auth/me").session(csrf.session()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void failedLoginDoesNotCreateAuthenticatedSession() throws Exception {
    Token csrf = token(null);
    mockMvc.perform(post("/api/auth/login").session(csrf.session())
        .header(csrf.header(), csrf.value()).contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"email":"missing-session@example.com","password":"Garage@2026"}
            """)).andExpect(status().isUnauthorized());
    mockMvc.perform(get("/api/auth/me").session(csrf.session()))
        .andExpect(status().isUnauthorized());
  }
}
