package com.worldgarage.backend.repository;

import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.model.ReviewStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {

  List<Car> findAllByReviewStatus(ReviewStatus reviewStatus);
  List<Car> findAllByOwnerEmailOrderByIdDesc(String email);
  Optional<Car> findByIdAndOwnerEmail(Long id, String email);
  List<Car> findAllByOwnerIdAndReviewStatusOrderByIdDesc(
      Long ownerId,
      ReviewStatus reviewStatus);
  boolean existsByImageUrlAndReviewStatus(
      String imageUrl,
      ReviewStatus reviewStatus);

  @EntityGraph(attributePaths = "owner")
  Optional<Car> findByIdAndReviewStatus(
      Long id,
      ReviewStatus reviewStatus
  );
}
