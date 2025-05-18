package com.ttn.nexuscart.service;

import com.ttn.nexuscart.entity.users.User;
import com.ttn.nexuscart.repositories.UserRepository;
import com.ttn.nexuscart.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Authenticating user with email: {}", email);
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            log.error("Authentication failed. User not found with email: {}", email);
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        log.info("Authentication successful for user: {}", email);
        return new CustomUserDetails(userOptional.get());
    }
}
