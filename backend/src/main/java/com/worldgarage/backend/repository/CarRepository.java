package com.worldgarage.backend.repository;

import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.model.ReviewStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {

  List<Car> findAllByReviewStatus(ReviewStatus reviewStatus);
  List<Car> findAllByOwnerEmailOrderByIdDesc(String email);

  Optional<Car> findByIdAndReviewStatus(
      Long id,
      ReviewStatus reviewStatus
  );
}
