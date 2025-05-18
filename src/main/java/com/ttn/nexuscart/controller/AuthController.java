package com.ttn.nexuscart.controller;

import com.ttn.nexuscart.co.*;
import com.ttn.nexuscart.vo.LoginResponseVO;
import com.ttn.nexuscart.exceptions.UnauthorisedAccessException;
import com.ttn.nexuscart.repositories.CustomerRepository;
import com.ttn.nexuscart.repositories.LoginTokenRepository;
import com.ttn.nexuscart.repositories.PasswordResetTokenRepository;
import com.ttn.nexuscart.repositories.UserRepository;
import com.ttn.nexuscart.security.jwt.JwtService;
import com.ttn.nexuscart.service.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    private LoginService loginService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordResetTokenRepository passwordResetRepo;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ResetPasswordService resetPasswordService;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private LoginTokenRepository loginTokenRepository;
    @Autowired
    private LogoutService logoutService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SellerService sellerService;

    @PostMapping("/login")
    public LoginResponseVO login(@RequestBody LoginRequestCO loginRequestCO) {
        log.info("Login attempt for user: {}", loginRequestCO.getEmail());
        return loginService.login(loginRequestCO);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseVO> refreshToken(@RequestBody RefreshTokenRequestCO request) {
        log.info("Refreshing token for refreshToken: {}", request.getRefreshToken());
        LoginResponseVO response = refreshTokenService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestCO request) {
        log.info("Password reset link requested for: {}", request.getEmail());
        return resetPasswordService.forgotPassword(request);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestCO request) {
        log.info("Attempting to reset password for token: {}", request.getToken());
        try {
            resetPasswordService.resetPassword(request);
            log.info("Password reset successful for token: {}", request.getToken());
            return ResponseEntity.ok("Password reset successfully.");
        } catch (IllegalArgumentException e) {
            log.error("Password reset failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Logout request received with token: {}", authHeader);
        return logoutService.logout(authHeader);
    }



    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_SELLER')")
    @PatchMapping("/update-password")
    public ResponseEntity<String> updatePassword(
            @Valid @RequestBody UpdatePasswordRequestCO request,
            Authentication authentication) {

        String email = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        log.info("Password update requested by user: {} with role: {}", email, role);

        if (role.equals("ROLE_CUSTOMER")) {
            customerService.updatePassword(request, authentication);
        } else if (role.equals("ROLE_SELLER")) {
            sellerService.updatePassword(request, authentication);
        } else {
            throw new UnauthorisedAccessException("You are not allowed to update the password.");
        }

        return ResponseEntity.ok("Password updated successfully.");
    }

}
