package com.ttn.nexuscart.service;

import com.ttn.nexuscart.entity.Token;
import com.ttn.nexuscart.entity.users.User;
import com.ttn.nexuscart.repositories.LoginTokenRepository;
import com.ttn.nexuscart.repositories.UserRepository;
import com.ttn.nexuscart.security.jwt.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LogoutService {

    private static final Logger logger = LoggerFactory.getLogger(LogoutService.class);
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final LoginTokenRepository loginTokenRepository;

    public LogoutService(JwtService jwtService, UserRepository userRepository, LoginTokenRepository loginTokenRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.loginTokenRepository = loginTokenRepository;
    }

    public ResponseEntity<String> logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Logout failed: Missing or invalid Authorization header");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or invalid Authorization header");
        }

        String accessToken = authHeader.substring(7);
        String email = jwtService.extractEmail(accessToken);

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        User user = userOptional.get();
        List<Token> userTokens = loginTokenRepository.findByUser(user);

        if (userTokens.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No session found for user.");
        }

        boolean allDeleted = userTokens.stream().allMatch(Token::isDeleted);

        if (allDeleted) {
            logger.info("User {} already logged out", user.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body("User already logged out.");
        }

        // Logout user by marking all tokens as deleted
        userTokens.forEach(token -> token.setDeleted(true));
        loginTokenRepository.saveAll(userTokens);

        logger.info("User {} logged out successfully", user.getEmail());
        return ResponseEntity.ok("User logged out successfully.");
    }
}

