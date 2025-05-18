package com.ttn.nexuscart.security.jwt;

import com.ttn.nexuscart.entity.Token;
import com.ttn.nexuscart.repositories.LoginTokenRepository;
import com.ttn.nexuscart.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    LoginTokenRepository loginTokenRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String email = null;
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            email = jwtService.extractEmail(token);
        }
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Fetch token from DB and check if it exists and is not deleted
            Optional<Token> storedTokenOpt = loginTokenRepository.findByToken(token);
            if (storedTokenOpt.isEmpty() || storedTokenOpt.get().isDeleted()) {
                filterChain.doFilter(request, response); // Token is invalid or deleted
                return;
            }
            Claims claims = jwtService.extractAllClaims(token);
            List<String> roles = claims.get("roles", List.class);

            List<SimpleGrantedAuthority> authorities = roles != null
                    ? roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(Collectors.toList())
                    : List.of();

            if (jwtService.validateToken(token)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                authorities
                        );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println(SecurityContextHolder.getContext().toString());
            }
        }
        filterChain.doFilter(request, response);
    }
}

