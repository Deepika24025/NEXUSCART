package com.ttn.nexuscart.service;

import com.ttn.nexuscart.co.ForgotPasswordRequestCO;
import com.ttn.nexuscart.co.ResetPasswordRequestCO;
import com.ttn.nexuscart.entity.PasswordResetToken;
import com.ttn.nexuscart.entity.users.User;
import com.ttn.nexuscart.repositories.PasswordResetTokenRepository;
import com.ttn.nexuscart.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ResetPasswordService {
    @Autowired
    private PasswordResetTokenRepository passwordResetRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;


    public ResponseEntity<?> forgotPassword(ForgotPasswordRequestCO request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found with this email.");
        }

        User user = optionalUser.get();
        passwordResetRepo.deleteAll(passwordResetRepo.findAll()  // Optional cleanup
                .stream().filter(t -> t.getUser().equals(user)).toList());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        passwordResetRepo.save(resetToken);

        String resetLink = "http://localhost:8080/auth/reset-password?token=" + token;
        emailService.sendResetEmail(user.getEmail(), "Reset your password using this link: " + resetLink);

        return ResponseEntity.ok("Reset password link has been sent to your email.");
    }


    public ResponseEntity<?> resetPassword(ResetPasswordRequestCO request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        PasswordResetToken token = passwordResetRepo.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token."));

        if (token.isExpired()) {
            throw new IllegalArgumentException("Token has expired.");
        }


        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        passwordResetRepo.delete(token);
        return ResponseEntity.ok("Password is reset successfully");
    }


}
