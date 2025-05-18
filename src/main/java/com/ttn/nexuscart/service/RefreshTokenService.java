package com.ttn.nexuscart.service;

import com.ttn.nexuscart.vo.LoginResponseVO;
import com.ttn.nexuscart.entity.Role;
import com.ttn.nexuscart.entity.users.User;
import com.ttn.nexuscart.repositories.CustomerRepository;
import com.ttn.nexuscart.security.jwt.JwtService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RefreshTokenService {
    private final JwtService jwtService;
    private final CustomerRepository customerRepository;

    public RefreshTokenService(JwtService jwtService, CustomerRepository customerRepository) {
        this.jwtService = jwtService;
        this.customerRepository = customerRepository;
    }

    public LoginResponseVO refreshToken(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String email = jwtService.extractEmail(refreshToken);
        User user = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> roles = user.getRoles().stream()
                .map(Role::getAuthority)
                .collect(Collectors.toList());

        String newAccessToken = jwtService.generateAccessToken(email, roles);
        return new LoginResponseVO(newAccessToken, refreshToken, "Token refreshed");
    }
}
