package com.worldgarage.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.worldgarage.backend.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CarControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CarRepository carRepository;

  @Test
  void createsCar() throws Exception {
    long carCountBefore = carRepository.count();

    mockMvc.perform(post("/api/cars")
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
        .andExpect(jsonPath("$.imageUrl").value("https://example.com/rx7.jpg"));

    assertEquals(carCountBefore + 1, carRepository.count());
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
  void returnsNotFoundWhenIdDoesNotExist() throws Exception {
    mockMvc.perform(get("/api/cars/99"))
        .andExpect(status().isNotFound());
  }
}
