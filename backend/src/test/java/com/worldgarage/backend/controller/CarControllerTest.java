package com.worldgarage.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CarControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void returnsCarWhenIdExists() throws Exception {
    mockMvc.perform(get("/api/cars/2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(2))
        .andExpect(jsonPath("$.make").value("Toyota"))
        .andExpect(jsonPath("$.model").value("Supra"));
  }

  @Test
  void returnsNotFoundWhenIdDoesNotExist() throws Exception {
    mockMvc.perform(get("/api/cars/99"))
        .andExpect(status().isNotFound());
  }
}
