package com.worldgarage.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.repository.UserAccountRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

 class UserAccountDetailsServiceTest {

    private UserAccountRepository userAccountRepository;
    private UserAccountDetailsService userAccountDetailsService;

    @BeforeEach
    void setup() {
        userAccountRepository = mock(UserAccountRepository.class);
        userAccountDetailsService =
            new UserAccountDetailsService(userAccountRepository);
        }

        @Test
        void loadUserWithNormalizedEmailAndRole() {
            //Arrange
            String passwordHash = "{bcrypt}test-hash";
            UserAccount user =
             new UserAccount("owner@example.com", passwordHash, "Garage Owner");

             when(userAccountRepository.findByEmail("owner@example.com"))
             .thenReturn(Optional.of(user));

            //Act
            UserDetails result = userAccountDetailsService.loadUserByUsername(" Owner@Example.com ");

            //Assert
            assertEquals("owner@example.com",result.getUsername());
            assertEquals(passwordHash, result.getPassword());
            assertEquals(1, result.getAuthorities().size());
           assertEquals(
    "ROLE_USER",
    result.getAuthorities().iterator().next().getAuthority());
            //Verify
            verify(userAccountRepository).findByEmail("owner@example.com");
        }

        @Test
        void throwsExceptionWhenEmailDoesNotExist() {
            //Arrange
            when(userAccountRepository.findByEmail("missing@example.com"))
            .thenReturn(Optional.empty());

            //Act & Assert
            UsernameNotFoundException exception =
            assertThrows(
                UsernameNotFoundException.class,
                () ->
                userAccountDetailsService.loadUserByUsername(
                    "missing@example.com"));

                    assertEquals("Invalid credentials", exception.getMessage());

                    //Verify
                    verify(userAccountRepository).findByEmail("missing@example.com");
        }

}
