package com.worldgarage.backend.controller;

import com.worldgarage.backend.dto.ApiErrorResponse;
import com.worldgarage.backend.dto.ImageUploadResponse;
import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.service.ImageAssetService;
import com.worldgarage.backend.service.UserAccountService;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class ImageUploadController {

  private final ImageAssetService imageAssetService;
  private final UserAccountService userAccountService;

  public ImageUploadController(
      ImageAssetService imageAssetService,
      UserAccountService userAccountService) {
    this.imageAssetService = imageAssetService;
    this.userAccountService = userAccountService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImageUploadResponse> uploadImage(
      @RequestPart("image") MultipartFile image,
      Authentication authentication)
      throws IOException {
    UserAccount owner =
        userAccountService
            .getUserByEmail(authentication.getName())
            .orElseThrow();

    String imageUrl =
        imageAssetService.store(image, owner);

    ImageUploadResponse response =
        new ImageUploadResponse(imageUrl);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidImage(
      IllegalArgumentException exception) {
    return ResponseEntity
        .badRequest()
        .body(new ApiErrorResponse(exception.getMessage()));
  }
}
