package com.worldgarage.backend.repository;

import com.worldgarage.backend.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {
}
