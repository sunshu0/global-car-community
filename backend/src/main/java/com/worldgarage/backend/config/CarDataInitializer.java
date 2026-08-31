package com.worldgarage.backend.config;

import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.repository.CarRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CarDataInitializer {

  @Bean
  CommandLineRunner loadCars(CarRepository carRepository) {
    return args -> {
      if (carRepository.count() == 0) {
        Car nissan = new Car(
            "Nissan",
            "370Z",
            "Auckland, New Zealand"
        );
        Car toyota = new Car(
            "Toyota",
            "Supra",
            "Tokyo, Japan"
        );
        Car bmw = new Car(
            "BMW",
            "M3",
            "Munich, Germany"
        );

        nissan.approve();
        toyota.approve();
        bmw.approve();

        carRepository.saveAll(List.of(nissan, toyota, bmw));
      }
    };
  }
}
