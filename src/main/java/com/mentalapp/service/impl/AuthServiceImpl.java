package com.mentalapp.service.impl;

import com.mentalapp.dto.*;
import com.mentalapp.model.User;
import com.mentalapp.repository.UserRepository;
import com.mentalapp.security.JwtUtil;
import com.mentalapp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        // Check if user already exists
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setIsEmailVerified(false);
        user.setFailedLoginAttempts(0);

        User savedUser = userRepository.save(user);

        // Create response
        AuthResponse response = new AuthResponse();
        response.setMessage("User registered successfully");

        return response;
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest req) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsernameOrEmail(), req.getPassword()));

        // Get user details
        User user = (User) authentication.getPrincipal();

        // Update last login
        user.setLastLoginAt(java.time.LocalDateTime.now());
        user.resetFailedLoginAttempts();
        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        // Create response
        AuthResponse response = new AuthResponse();
        response.setAccessToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(900000L); // 15 minutes
        response.setMessage("Login successful");

        return response;
    }

    @Override
    @Transactional
    public AuthResponse googleAuth(GoogleAuthRequest req) {
        try {
            // For now, we'll implement a simple Google token validation
            // In a real implementation, you would validate the Google ID token
            // and extract user information from it

            // Create or find user based on Google token
            // This is a simplified implementation - in production you'd validate the token
            User user = userRepository.findByEmail(req.getToken()) // Using token as email for now
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(req.getToken()); // In real implementation, extract from Google token
                        newUser.setUsername("google_" + System.currentTimeMillis());
                        newUser.setPasswordHash(""); // No password for OAuth users
                        newUser.setIsEmailVerified(true);
                        newUser.setFailedLoginAttempts(0);
                        newUser.setExternalId(req.getToken()); // Store Google ID
                        newUser.setAuthProvider("GOOGLE");
                        return userRepository.save(newUser);
                    });

            // Update last login
            user.setLastLoginAt(java.time.LocalDateTime.now());
            userRepository.save(user);

            // Generate JWT token
            String token = jwtUtil.generateToken(user);

            // Create response
            AuthResponse response = new AuthResponse();
            response.setAccessToken(token);
            response.setTokenType("Bearer");
            response.setExpiresIn(900000L); // 15 minutes
            response.setMessage("Google authentication successful");

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {
            return userRepository.findByUsername(authentication.getName())
                    .orElse(null);
        }
        return null;
    }
}
