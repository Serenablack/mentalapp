package com.mentalapp.service.implementations;

import com.mentalapp.dto.*;
import com.mentalapp.mapper.UserMapper;
import com.mentalapp.model.User;
import com.mentalapp.repository.UserRepository;
import com.mentalapp.security.JwtUtil;
import com.mentalapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public User registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

//    @Override
//    @Transactional
//    public User registerOAuthUser(String email, String name, String provider) {
//        // Check if user already exists with this email
//        if (userRepository.existsByEmail(email)) {
//            return userRepository.findByEmail(email)
//                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//        }
//
//        // Create new OAuth user
//        User user = new User();
//        user.setEmail(email);
//        user.setUsername(email.split("@")[0]); // Use email prefix as username
//        user.setFirstName(name);
//        user.setLastName("");
//        user.setIsEmailVerified(true); // OAuth users are pre-verified
//        user.setPasswordHash(""); // No password for OAuth users
//
//        return userRepository.save(user);
//    }

    @Override
    @Transactional
    public ResponseEntity<?> authenticateUser(UserLoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));

        // Try to find user by email first, then by username if not found
        User user = userRepository.findByEmail(request.getUsernameOrEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(token));
    }

//    @Override
//    @Transactional
//    public AuthResponse googleAuth(GoogleAuthRequest request) {
//        try {
//            // For now, we'll implement a simple Google token validation
//            // In a real implementation, you would validate the Google ID token
//            // and extract user information from it
//
//            // Create or find user based on Google token
//            // This is a simplified implementation - in production you'd validate the token
//            User user = userRepository.findByEmail(request.getToken()) // Using token as email for now
//                    .orElseGet(() -> {
//                        User newUser = new User();
//                        newUser.setEmail(request.getToken()); // In real implementation, extract from Google token
//                        newUser.setUsername("google_" + System.currentTimeMillis());
//                        newUser.setPasswordHash(""); // No password for OAuth users
//                        newUser.setIsEmailVerified(true);
//                        newUser.setFailedLoginAttempts(0);
//                        newUser.setExternalId(request.getToken()); // Store Google ID
//                        newUser.setAuthProvider("GOOGLE");
//                        return userRepository.save(newUser);
//                    });
//
//            // Update last login
//            user.setLastLoginAt(LocalDateTime.now());
//            userRepository.save(user);
//
//            // Generate JWT token
//            String token = jwtUtil.generateToken(user);
//
//            // Create response
//            AuthResponse response = new AuthResponse();
//            response.setAccessToken(token);
//            response.setTokenType("Bearer");
//            response.setExpiresIn(86400L); // 24 hours
//            response.setMessage("Google authentication successful");
//
//            return response;
//        } catch (Exception e) {
//            throw new RuntimeException("Google authentication failed: " + e.getMessage());
//        }
//    }

//    @Override
//    @Transactional(readOnly = true)
//    public UserProfileResponse getUserProfile(Long userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        return userMapper.toProfileResponse(user);
//    }
//
//    @Override
//    @Transactional
//    public UserProfileResponse updateUserProfile(Long userId, UserUpdateRequest request) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        userMapper.updateEntity(user, request);
//        user = userRepository.save(user);
//
//        return userMapper.toProfileResponse(user);
//    }

//    @Override
//    @Transactional
//    public void verifyEmail(String token) {
//        User user = userRepository.findByEmailVerificationToken(token)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));
//
//        if (user.getEmailVerificationExpiresAt().isBefore(LocalDateTime.now())) {
//            throw new IllegalArgumentException("Verification token has expired");
//        }
//
//        user.setIsEmailVerified(true);
//        user.setEmailVerificationToken(null);
//        user.setEmailVerificationExpiresAt(null);
//        userRepository.save(user);
//    }
//
//    @Override
//    @Transactional
//    public void requestPasswordReset(String email) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        String token = UUID.randomUUID().toString();
//        user.setPasswordResetToken(token);
//        user.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(24));
//        userRepository.save(user);
//
//        // TODO: Send password reset email
//    }
//
//    @Override
//    @Transactional
//    public void resetPassword(String token, String newPassword) {
//        User user = userRepository.findByPasswordResetToken(token)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));
//
//        if (user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
//            throw new IllegalArgumentException("Reset token has expired");
//        }
//
//        user.setPasswordHash(passwordEncoder.encode(newPassword));
//        user.setPasswordResetToken(null);
//        user.setPasswordResetExpiresAt(null);
//        userRepository.save(user);
//    }

}