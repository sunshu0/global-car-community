package com.worldgarage.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.worldgarage.backend.exception.EmailAlreadyRegisteredException;
import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.model.UserRole;
import com.worldgarage.backend.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserAccountServiceTest {
  @Test
  void findsUserByNormalizedEmail() {
    UserAccount user = new UserAccount("owner@example.com", "hash", "Owner");
    when(userAccountRepository.findByEmail("owner@example.com"))
        .thenReturn(java.util.Optional.of(user));
    assertSame(user, userAccountService.getUserByEmail(" Owner@Example.com ").orElseThrow());
    verify(userAccountRepository).findByEmail("owner@example.com");
  }

  @Test
  void returnsEmptyWhenUserDoesNotExist() {
    when(userAccountRepository.findByEmail("missing@example.com"))
        .thenReturn(java.util.Optional.empty());
    assertEquals(java.util.Optional.empty(),
        userAccountService.getUserByEmail("missing@example.com"));
    verify(userAccountRepository).findByEmail("missing@example.com");
  }

  @Test
  void findsUserById() {
    UserAccount user =
        new UserAccount(
            "owner@example.com",
            "password-hash",
            "Garage Owner");

    when(userAccountRepository.findById(3L))
        .thenReturn(java.util.Optional.of(user));

    UserAccount result =
        userAccountService.getUserById(3L).orElseThrow();

    assertSame(user, result);

    verify(userAccountRepository).findById(3L);
  }

  @Test
  void returnsEmptyWhenUserIdDoesNotExist() {
    when(userAccountRepository.findById(99L))
        .thenReturn(java.util.Optional.empty());

    java.util.Optional<UserAccount> result =
        userAccountService.getUserById(99L);

    assertEquals(java.util.Optional.empty(), result);

    verify(userAccountRepository).findById(99L);
  }

  private UserAccountRepository userAccountRepository;
  private PasswordEncoder passwordEncoder;
  private UserAccountService userAccountService;

  @BeforeEach
  void setup() {
    userAccountRepository = mock(UserAccountRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);

    userAccountService = new UserAccountService(userAccountRepository, passwordEncoder);
  }

  @Test
  void registersUserWithNormalizedEmailAndEncodedPassword() {
    String passwordHash = "{bcrypt}encoded-password";

    when(userAccountRepository.existsByEmail("owner@example.com")).thenReturn(false);
    when(passwordEncoder.encode("Garage@2026")).thenReturn(passwordHash);

    UserAccount savedUser =
        new UserAccount("owner@example.com", passwordHash, "Garage Owner");

    when(userAccountRepository.save(any(UserAccount.class))).thenReturn(savedUser);

    UserAccount registeredUser =
        userAccountService.register(
            " Owner@Example.com ", "Garage@2026", " Garage Owner ");

    assertSame(savedUser, registeredUser);

    ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
    verify(userAccountRepository).save(userCaptor.capture());

    UserAccount userPassedToRepository = userCaptor.getValue();

    assertEquals("owner@example.com", userPassedToRepository.getEmail());
    assertEquals(passwordHash, userPassedToRepository.getPasswordHash());
    assertEquals("Garage Owner", userPassedToRepository.getDisplayName());
    assertEquals(UserRole.USER, userPassedToRepository.getRole());

    verify(userAccountRepository).existsByEmail("owner@example.com");
    verify(passwordEncoder).encode("Garage@2026");
  }

  @Test
  void rejectsRegistrationIfEmailAlreadyExists() {
    when(userAccountRepository.existsByEmail("owner@example.com")).thenReturn(true);

    EmailAlreadyRegisteredException exception =
        assertThrows(
            EmailAlreadyRegisteredException.class,
            () ->
                userAccountService.register(
                    "Owner@Example.com", "Garage@2026", "Garage Owner"));

    assertEquals("Email already exists: owner@example.com", exception.getMessage());

    verify(userAccountRepository).existsByEmail("owner@example.com");
    verify(passwordEncoder, never()).encode(any(CharSequence.class));
    verify(userAccountRepository, never()).save(any(UserAccount.class));
  }
}
