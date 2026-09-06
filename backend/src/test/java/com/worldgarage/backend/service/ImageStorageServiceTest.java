package com.worldgarage.backend.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class ImageStorageServiceTest {

  @TempDir
  Path temporaryDirectory;

  @Test
  void storesJpegImage() throws Exception {
    byte[] imageBytes = "fake-jpeg-content".getBytes();

    MockMultipartFile image =
        new MockMultipartFile(
            "image",
            "car.jpg",
            "image/jpeg",
            imageBytes);

    ImageStorageService imageStorageService =
        new ImageStorageService(
            temporaryDirectory.toString());

    String imageUrl = imageStorageService.store(image);

    assertTrue(imageUrl.startsWith("/uploads/"));
    assertTrue(imageUrl.endsWith(".jpg"));

    String fileName =
        imageUrl.substring("/uploads/".length());

    Path savedFile =
        temporaryDirectory.resolve(fileName);

    assertTrue(Files.exists(savedFile));
    assertArrayEquals(
        imageBytes,
        Files.readAllBytes(savedFile));
  }
}
