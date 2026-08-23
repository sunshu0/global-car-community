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

  protected Car() {
  }

  public Car(String make, String model, String location) {
    this(null, make, model, location);
  }

  public Car(Long id, String make, String model, String location) {
    this.id = id;
    this.make = make;
    this.model = model;
    this.location = location;
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
}
