package com.worldgarage.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "cars")
public class Car {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String make;

  @Column(nullable = false)
  private String model;

  @Column(nullable = false)
  private String location;

  @Column(length = 2048)
  private String imageUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @ColumnDefault("'APPROVED'")
  private ReviewStatus reviewStatus = ReviewStatus.PENDING;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  private UserAccount owner;

  protected Car() {
  }

  public Car(String make, String model, String location) {
    this(make, model, location, null);
  }

  public Car(String make, String model, String location, String imageUrl) {
    this(null, make, model, location, imageUrl);
  }

  public Car(Long id, String make, String model, String location) {
    this(id, make, model, location, null);
  }

  public Car(Long id, String make, String model, String location, String imageUrl) {
    this.id = id;
    this.make = make;
    this.model = model;
    this.location = location;
    this.imageUrl = imageUrl;
  }

  public void approve() {
    reviewStatus = ReviewStatus.APPROVED;
  }

  public void reject() {
    reviewStatus = ReviewStatus.REJECTED;
  }

  public Long getId() {
    return id;
  }

  public String getMake() {
    return make;
  }

  public String getModel() {
    return model;
  }

  public String getLocation() {
    return location;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public ReviewStatus getReviewStatus() {
    return reviewStatus;
  }

  public UserAccount getOwner() {
    return owner;
  }

  public void setOwner(UserAccount owner) {
    this.owner = owner;
  }
}
