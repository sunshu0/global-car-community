package com.worldgarage.backend.config;

import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.repository.UserAccountRepository;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

  private final UserAccountRepository userAccountRepository;
  private final PasswordEncoder passwordEncoder;
  private final String email;
  private final String password;
  private final String displayName;

  public AdminAccountInitializer(
      UserAccountRepository userAccountRepository,
      PasswordEncoder passwordEncoder,
      @Value("${worldgarage.bootstrap.admin.email:}") String email,
      @Value("${worldgarage.bootstrap.admin.password:}") String password,
      @Value("${worldgarage.bootstrap.admin.display-name:World Garage Admin}")
          String displayName) {
    this.userAccountRepository = userAccountRepository;
    this.passwordEncoder = passwordEncoder;
    this.email = email;
    this.password = password;
    this.displayName = displayName;
  }

  @Override
  public void run(String... args) {
    if (email.isBlank() && password.isBlank()) {
      return;
    }

    if (email.isBlank() || password.isBlank()) {
      throw new IllegalStateException(
          "Both admin email and password must be configured.");
    }

    if (password.length() < 8 || password.length() > 72) {
      throw new IllegalStateException(
          "The bootstrap admin password must contain 8 to 72 characters.");
    }

    String normalizedEmail =
        email.toLowerCase(Locale.ROOT).trim();

    UserAccount admin =
        userAccountRepository
            .findByEmail(normalizedEmail)
            .orElseGet(
                () ->
                    new UserAccount(
                        normalizedEmail,
                        passwordEncoder.encode(password),
                        normalizedDisplayName()));

    admin.promoteToAdmin();
    userAccountRepository.save(admin);
  }

  private String normalizedDisplayName() {
    String normalized = displayName.trim();
    return normalized.isEmpty() ? "World Garage Admin" : normalized;
  }
}
