package com.worldgarage.backend.service;

import com.worldgarage.backend.model.UserAccount;
import com.worldgarage.backend.repository.UserAccountRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserAccountDetailsService implements UserDetailsService{

    private final UserAccountRepository userAccountRepository;

    public UserAccountDetailsService(
        UserAccountRepository userAccountRepository){
            this.userAccountRepository = userAccountRepository;
        }

        @Override
        public UserDetails loadUserByUsername(String email) {
            String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();

            UserAccount user =
            userAccountRepository
            .findByEmail(normalizedEmail)
            .orElseThrow(
                () -> new UsernameNotFoundException("Invalid credentials"));

                return User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();

            }

}
