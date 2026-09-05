package com.worldgarage.backend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserAuthenticationTest {

    @Autowired private UserAccountRepository userAccountRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private AuthenticationManager authenticationManager;

    @Test
    void authenticatesDatabaseUserWithCorrectPassword() throws Exception {
        //Arrange
        String email = "authentication@example.com";
        String rawPassword = "Garage@2026";
        String passwordHash = passwordEncoder.encode(rawPassword);

        userAccountRepository.save(
            new UserAccount(email,passwordHash,"Garage owner"));

        //Act
        Authentication result =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, rawPassword));

        //Assert
        assertTrue(result.isAuthenticated());
        assertEquals(email, result.getName());
        assertThat(result.getAuthorities())
            .extracting(authority -> authority.getAuthority())
            .containsExactlyInAnyOrder("ROLE_USER", "FACTOR_PASSWORD");
    }

    @Test
    void rejectsDatabaseUserWithWrongPassword() throws Exception {
        //Arrange
        String email = "wrong-password@example.com";
        String passwordHash = passwordEncoder.encode("Garage@2026");

        userAccountRepository.save(new UserAccount(email,passwordHash,"Garage Owner"));


        //Act & Assert
        assertThrows(
            BadCredentialsException.class,
            () ->
                authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                        email, "WrongPassword")));
    }

}
