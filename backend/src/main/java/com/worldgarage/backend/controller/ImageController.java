package com.worldgarage.backend.controller;

import com.worldgarage.backend.service.ImageAssetService;
import com.worldgarage.backend.service.ImageAssetService.ImageDownload;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
public class ImageController {

  private final ImageAssetService imageAssetService;

  public ImageController(
      ImageAssetService imageAssetService) {
    this.imageAssetService = imageAssetService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getImage(
      @PathVariable UUID id,
      Authentication authentication)
      throws IOException {
    Optional<ImageDownload> image =
        imageAssetService.findVisibleImage(
            id,
            authentication);

    if (image.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    ImageDownload download = image.get();

    return ResponseEntity.ok()
        .contentType(
            MediaType.parseMediaType(
                download.contentType()))
        .body(download.resource());
  }
}
