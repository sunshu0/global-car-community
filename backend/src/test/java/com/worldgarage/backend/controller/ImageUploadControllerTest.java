package com.worldgarage.backend.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.worldgarage.backend.model.Car;
import com.worldgarage.backend.model.ImageAsset;
import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.repository.CarRepository;
import com.worldgarage.backend.repository.ImageAssetRepository;
import com.worldgarage.backend.repository.UserAccountRepository;
import com.worldgarage.backend.service.ImageAssetService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties =
        "worldgarage.upload.directory=${java.io.tmpdir}/worldgarage-image-tests")
@AutoConfigureMockMvc
@Transactional
class ImageUploadControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserAccountRepository userAccountRepository;

  @Autowired
  private ImageAssetRepository imageAssetRepository;

  @Autowired
  private ImageAssetService imageAssetService;

  @Autowired
  private CarRepository carRepository;

  @Test
  @WithMockUser(username = "image-owner@example.com", roles = "USER")
  void uploadsImageAndProtectsItUntilApproval() throws Exception {
    userAccountRepository.save(
        new UserAccount(
            "image-owner@example.com",
            "{bcrypt}test-password-hash",
            "Image Owner"));

    byte[] imageBytes = "test-jpeg-content".getBytes();
    MockMultipartFile image =
        new MockMultipartFile(
            "image",
            "garage.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            imageBytes);

    MvcResult uploadResult =
        mockMvc
            .perform(
                multipart("/api/uploads")
                    .file(image)
                    .with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.imageUrl").value(org.hamcrest.Matchers.startsWith("/api/images/")))
            .andReturn();

    String imageUrl =
        JsonPath.read(
            uploadResult.getResponse().getContentAsString(),
            "$.imageUrl");

    mockMvc
        .perform(get(imageUrl))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_JPEG))
        .andExpect(content().bytes(imageBytes));

    mockMvc
        .perform(get(imageUrl).with(anonymous()))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(get(imageUrl).with(user("admin@example.com").roles("ADMIN")))
        .andExpect(status().isOk());
  }

  @Test
  void rejectsAnonymousUpload() throws Exception {
    MockMultipartFile image =
        new MockMultipartFile(
            "image",
            "garage.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image".getBytes());

    mockMvc
        .perform(
            multipart("/api/uploads")
                .file(image)
                .with(csrf()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "invalid-image@example.com", roles = "USER")
  void rejectsUnsupportedFileType() throws Exception {
    userAccountRepository.save(
        new UserAccount(
            "invalid-image@example.com",
            "{bcrypt}test-password-hash",
            "Invalid Image Owner"));

    MockMultipartFile textFile =
        new MockMultipartFile(
            "image",
            "notes.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "not-an-image".getBytes());

    mockMvc
        .perform(
            multipart("/api/uploads")
                .file(textFile)
                .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.message")
                .value("Only JPEG, PNG, and WebP images are supported."));
  }

  @Test
  void makesImagePublicAfterAssociatedCarIsApproved() throws Exception {
    UserAccount owner =
        userAccountRepository.save(
            new UserAccount(
                "approved-image@example.com",
                "{bcrypt}test-password-hash",
                "Approved Image Owner"));

    byte[] imageBytes = "approved-jpeg-content".getBytes();
    MockMultipartFile image =
        new MockMultipartFile(
            "image",
            "approved.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            imageBytes);
    String imageUrl = imageAssetService.store(image, owner);

    Car car =
        new Car(
            "Nissan",
            "370Z",
            "Auckland, New Zealand",
            imageUrl);
    car.setOwner(owner);
    car.approve();
    carRepository.save(car);

    mockMvc
        .perform(get(imageUrl).with(anonymous()))
        .andExpect(status().isOk())
        .andExpect(content().bytes(imageBytes));
  }

  @Test
  @WithMockUser(username = "other-owner@example.com", roles = "USER")
  void rejectsCarSubmissionUsingAnotherOwnersImage() throws Exception {
    UserAccount imageOwner =
        userAccountRepository.save(
            new UserAccount(
                "original-owner@example.com",
                "{bcrypt}test-password-hash",
                "Original Owner"));
    userAccountRepository.save(
        new UserAccount(
            "other-owner@example.com",
            "{bcrypt}test-password-hash",
            "Other Owner"));

    UUID imageId = UUID.randomUUID();
    imageAssetRepository.save(
        new ImageAsset(
            imageId,
            "/uploads/" + imageId + ".jpg",
            MediaType.IMAGE_JPEG_VALUE,
            imageOwner));

    mockMvc
        .perform(
            post("/api/cars")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "make": "Mazda",
                      "model": "RX-7",
                      "location": "Hiroshima, Japan",
                      "imageUrl": "/api/images/%s"
                    }
                    """
                        .formatted(imageId)))
        .andExpect(status().isBadRequest());
  }
}
