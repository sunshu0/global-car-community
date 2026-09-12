package com.worldgarage.backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.model.UserRole;
import com.worldgarage.backend.repository.UserAccountRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminAccountInitializerTest {

  private UserAccountRepository userAccountRepository;
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    userAccountRepository = mock(UserAccountRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
  }

  @Test
  void doesNothingWhenBootstrapCredentialsAreNotConfigured() {
    AdminAccountInitializer initializer = initializer("", "", "");

    initializer.run();

    verify(userAccountRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void createsConfiguredAdministratorWithEncodedPassword() {
    when(userAccountRepository.findByEmail("admin@example.com"))
        .thenReturn(Optional.empty());
    when(passwordEncoder.encode("AdminPassword2026"))
        .thenReturn("{bcrypt}encoded-password");

    AdminAccountInitializer initializer =
        initializer(
            " Admin@Example.com ",
            "AdminPassword2026",
            "Garage Administrator");

    initializer.run();

    ArgumentCaptor<UserAccount> captor =
        ArgumentCaptor.forClass(UserAccount.class);
    verify(userAccountRepository).save(captor.capture());

    UserAccount admin = captor.getValue();
    assertEquals("admin@example.com", admin.getEmail());
    assertEquals("{bcrypt}encoded-password", admin.getPasswordHash());
    assertEquals("Garage Administrator", admin.getDisplayName());
    assertEquals(UserRole.ADMIN, admin.getRole());
  }

  @Test
  void promotesExistingAccountWithoutResettingItsPassword() {
    UserAccount existing =
        new UserAccount(
            "owner@example.com",
            "{bcrypt}existing-password",
            "Existing Owner");
    when(userAccountRepository.findByEmail("owner@example.com"))
        .thenReturn(Optional.of(existing));

    initializer(
        "owner@example.com",
        "BootstrapPassword2026",
        "Ignored Name")
        .run();

    assertEquals(UserRole.ADMIN, existing.getRole());
    assertEquals("{bcrypt}existing-password", existing.getPasswordHash());
    verify(passwordEncoder, never()).encode("BootstrapPassword2026");
    verify(userAccountRepository).save(existing);
  }

  @Test
  void rejectsPartialBootstrapConfiguration() {
    AdminAccountInitializer initializer =
        initializer("admin@example.com", "", "Admin");

    assertThrows(IllegalStateException.class, initializer::run);
  }

  private AdminAccountInitializer initializer(
      String email,
      String password,
      String displayName) {
    return new AdminAccountInitializer(
        userAccountRepository,
        passwordEncoder,
        email,
        password,
        displayName);
  }
}
