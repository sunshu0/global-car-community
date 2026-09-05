package com.worldgarage.backend.service;

import com.worldgarage.backend.exception.EmailAlreadyRegisteredException;
import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.repository.UserAccountRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserAccountService {

  private final UserAccountRepository userAccountRepository;
  private final PasswordEncoder passwordEncoder;

  public UserAccountService(
      UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
    this.userAccountRepository = userAccountRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public UserAccount register(String email, String rawPassword, String displayName) {
    String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();

    if (userAccountRepository.existsByEmail(normalizedEmail)) {
      throw new EmailAlreadyRegisteredException(normalizedEmail);
    }

    String passwordHash = passwordEncoder.encode(rawPassword);

    UserAccount user = new UserAccount(normalizedEmail, passwordHash, displayName.trim());
    return userAccountRepository.save(user);
  }

  public Optional<UserAccount> getUserByEmail(String email) {
    String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();
    return userAccountRepository.findByEmail(normalizedEmail);
  }
}
