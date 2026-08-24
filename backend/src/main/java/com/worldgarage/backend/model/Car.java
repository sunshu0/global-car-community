package com.worldgarage.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
}
