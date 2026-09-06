package com.worldgarage.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService {

  private final Path uploadDirectory;

  public ImageStorageService(
      @Value("${worldgarage.upload.directory:./uploads}")
      String uploadDirectory) {

    this.uploadDirectory =
        Path.of(uploadDirectory)
            .toAbsolutePath()
            .normalize();
  }

  public String store(MultipartFile file) throws IOException {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("Image file is required.");
    }

    String contentType = file.getContentType();

    if (contentType == null) {
      throw new IllegalArgumentException(
          "Only JPEG, PNG, and WebP images are supported.");
    }

    String extension =
        switch (contentType) {
          case "image/jpeg" -> ".jpg";
          case "image/png" -> ".png";
          case "image/webp" -> ".webp";
          default ->
              throw new IllegalArgumentException(
                  "Only JPEG, PNG, and WebP images are supported.");
        };

    Files.createDirectories(uploadDirectory);

    String fileName = UUID.randomUUID() + extension;
    Path destination = uploadDirectory.resolve(fileName);

    file.transferTo(destination);

    return "/uploads/" + fileName;
  }

  public Resource load(String storagePath) throws IOException {
    if (!storagePath.startsWith("/uploads/")) {
      throw new IOException("Invalid image storage path.");
    }

    String fileName =
        storagePath.substring("/uploads/".length());

    Path imagePath =
        uploadDirectory.resolve(fileName)
            .normalize();

    if (!imagePath.startsWith(uploadDirectory)
        || !Files.isRegularFile(imagePath)) {
      throw new IOException("Image file could not be found.");
    }

    return new FileSystemResource(imagePath);
  }
}
