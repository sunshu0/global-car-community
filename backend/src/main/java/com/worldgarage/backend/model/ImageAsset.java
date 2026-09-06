package com.worldgarage.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "image_assets")
public class ImageAsset {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true, length = 255)
  private String storagePath;

  @Column(nullable = false, length = 100)
  private String contentType;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "owner_id", nullable = false)
  private UserAccount owner;

  protected ImageAsset() {}

  public ImageAsset(
      UUID id,
      String storagePath,
      String contentType,
      UserAccount owner) {
    this.id = id;
    this.storagePath = storagePath;
    this.contentType = contentType;
    this.owner = owner;
  }

  public UUID getId() {
    return id;
  }

  public String getStoragePath() {
    return storagePath;
  }

  public String getContentType() {
    return contentType;
  }

  public UserAccount getOwner() {
    return owner;
  }
}
