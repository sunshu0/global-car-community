package com.worldgarage.backend.service;

import com.worldgarage.backend.model.ImageAsset;
import com.worldgarage.backend.model.ReviewStatus;
import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.repository.CarRepository;
import com.worldgarage.backend.repository.ImageAssetRepository;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageAssetService {

  private final ImageStorageService imageStorageService;
  private final ImageAssetRepository imageAssetRepository;
  private final CarRepository carRepository;

  public ImageAssetService(
      ImageStorageService imageStorageService,
      ImageAssetRepository imageAssetRepository,
      CarRepository carRepository) {
    this.imageStorageService = imageStorageService;
    this.imageAssetRepository = imageAssetRepository;
    this.carRepository = carRepository;
  }

  public String store(
      MultipartFile file,
      UserAccount owner)
      throws IOException {
    String storagePath = imageStorageService.store(file);
    UUID id = UUID.randomUUID();

    ImageAsset imageAsset =
        new ImageAsset(
            id,
            storagePath,
            file.getContentType(),
            owner);

    imageAssetRepository.save(imageAsset);

    return publicUrl(id);
  }

  public Optional<ImageDownload> findVisibleImage(
      UUID id,
      Authentication authentication)
      throws IOException {
    Optional<ImageAsset> imageAsset =
        imageAssetRepository.findById(id);

    if (imageAsset.isEmpty()) {
      return Optional.empty();
    }

    ImageAsset asset = imageAsset.get();

    if (!isPublic(asset)
        && !isOwner(asset, authentication)
        && !isAdmin(authentication)) {
      return Optional.empty();
    }

    Resource resource =
        imageStorageService.load(asset.getStoragePath());

    return Optional.of(
        new ImageDownload(
            resource,
            asset.getContentType()));
  }

  public boolean canUseImage(
      String imageUrl,
      UserAccount owner) {
    if (imageUrl == null || imageUrl.isBlank()) {
      return true;
    }

    String imagePrefix = "/api/images/";

    if (!imageUrl.startsWith(imagePrefix)) {
      return true;
    }

    try {
      UUID imageId =
          UUID.fromString(imageUrl.substring(imagePrefix.length()));

      return imageAssetRepository
          .findById(imageId)
          .map(
              imageAsset ->
                  Objects.equals(
                      imageAsset.getOwner().getId(),
                      owner.getId()))
          .orElse(false);
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }

  private boolean isPublic(ImageAsset asset) {
    return carRepository.existsByImageUrlAndReviewStatus(
        publicUrl(asset.getId()),
        ReviewStatus.APPROVED);
  }

  private boolean isOwner(
      ImageAsset asset,
      Authentication authentication) {
    return authentication != null
        && authentication.isAuthenticated()
        && asset.getOwner()
            .getEmail()
            .equalsIgnoreCase(authentication.getName());
  }

  private boolean isAdmin(Authentication authentication) {
    return authentication != null
        && authentication.getAuthorities().stream()
            .anyMatch(
                authority ->
                    authority.getAuthority()
                        .equals("ROLE_ADMIN"));
  }

  private String publicUrl(UUID id) {
    return "/api/images/" + id;
  }

  public record ImageDownload(
      Resource resource,
      String contentType) {}
}
