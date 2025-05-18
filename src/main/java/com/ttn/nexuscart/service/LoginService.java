package com.ttn.nexuscart.service;


import com.ttn.nexuscart.co.LoginRequestCO;
import com.ttn.nexuscart.vo.LoginResponseVO;
import com.ttn.nexuscart.entity.PasswordResetToken;
import com.ttn.nexuscart.entity.Role;
import com.ttn.nexuscart.entity.Token;
import com.ttn.nexuscart.entity.users.User;
import com.ttn.nexuscart.enums.TokenType;
import com.ttn.nexuscart.repositories.LoginTokenRepository;
import com.ttn.nexuscart.repositories.PasswordResetTokenRepository;
import com.ttn.nexuscart.repositories.UserRepository;
import com.ttn.nexuscart.security.jwt.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LoginService {
    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordResetTokenRepository passwordResetRepo;
    @Autowired
    private LoginTokenRepository loginTokenRepository;

    @Autowired
    public LoginService(UserRepository userRepository,
                        JwtService jwtService,
                        PasswordEncoder passwordEncoder) {
        logger.info("Login attempted");
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponseVO login(LoginRequestCO request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User does not exist");
        }

        User user = optionalUser.get();
//        if (user.isPasswordExpired()) {
//            throw new BadCredentialsException("Password has been expired");
//        }
        if (user.getIsLocked()) {
            logger.warn("Login failed: Account is locked for email {}", user.getEmail());
            throw new RuntimeException("Account is locked");
        }

        if (!user.getIsActive()) {
            logger.warn("Login failed: User is not active for email {}", user.getEmail());
            throw new RuntimeException("User is not active");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.setInvalidAttemptCount(user.getInvalidAttemptCount() + 1);

            if (user.getInvalidAttemptCount() >= 3) {
                user.setIsLocked(true);
                emailService.sendAccountLocked(user.getEmail());
                userRepository.save(user);
                logger.error("Account locked due to 3 invalid attempts for user {}", user.getEmail());
                throw new RuntimeException("Account Locked");
            }

            userRepository.save(user);
            throw new RuntimeException("Invalid credentials");
        }

        user.setInvalidAttemptCount(0);
        userRepository.save(user);

        List<String> roles = user.getRoles().stream()
                .map(Role::getAuthority)
                .collect(Collectors.toList());

        String accessToken = jwtService.generateAccessToken(user.getEmail(), roles);
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        logger.info("Generated JWT tokens for user {}", user.getEmail());

        LocalDateTime accessTokenExpiry = LocalDateTime.now().plusMinutes(15);  // for example, 15 minutes
        LocalDateTime refreshTokenExpiry = LocalDateTime.now().plusHours(24);

        Token accessTokenEntity = new Token(accessToken, TokenType.ACCESS, accessTokenExpiry, user);
        Token refreshTokenEntity = new Token(refreshToken, TokenType.REFRESH, refreshTokenExpiry, user);
        loginTokenRepository.save(accessTokenEntity);
        loginTokenRepository.save(refreshTokenEntity);
        logger.info("Saved access and refresh tokens for user {}", user.getEmail());
        return new LoginResponseVO(accessToken, refreshToken, "Login successfull");
    }


    public void processForgotPassword(String email) {
        logger.info("Processing forgot password for email: {}", email);
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() || !userOpt.get().getIsActive()) {
            logger.warn("Invalid or inactive user trying to reset password: {}", email);
            throw new IllegalArgumentException("Invalid or inactive user.");
        }

        User user = userOpt.get();

        // Delete any existing token
        passwordResetRepo.deleteByUser(user);
        logger.info("Deleted existing password reset tokens for user {}", user.getEmail());

        // Generate a new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        passwordResetRepo.save(resetToken);
        logger.info("Generated new password reset token for user {}", user.getEmail());

        //  reset link
        String resetLink = "http://localhost:8080/reset-password?token=" + token;

        emailService.sendResetEmail(
                user.getEmail(),
                resetLink
        );
        logger.info("Sent password reset email to {}", user.getEmail());
    }


}
