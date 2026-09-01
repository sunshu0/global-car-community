package com.worldgarage.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.worldgarage.backend.model.UserAccount;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserAccountRepositoryTest {

  @Autowired private UserAccountRepository userAccountRepository;

  @Test
  void savesAndFindsUserByEmail() {
    UserAccount user = new UserAccount("owner@example.com", "encoded-password", "Garage Owner");

    UserAccount savedUser = userAccountRepository.save(user);
    Optional<UserAccount> foundUser = userAccountRepository.findByEmail("owner@example.com");

    assertNotNull(savedUser.getId());
    assertTrue(foundUser.isPresent());
    assertEquals(savedUser.getId(), foundUser.get().getId());
    assertTrue(userAccountRepository.existsByEmail("owner@example.com"));
    assertFalse(userAccountRepository.existsByEmail("missing@example.com"));
  }
}
