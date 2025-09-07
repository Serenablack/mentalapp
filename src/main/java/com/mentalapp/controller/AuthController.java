package com.mentalapp.controller;

import com.mentalapp.dto.*;
import com.mentalapp.model.User;
import com.mentalapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    /**
     * User registration endpoint
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {
        log.info("Registration request received for username: {}", request.getUsername());

        try {
            User result= userService.registerUser(request);
            log.info("User registered successfully: {}", request.getUsername());
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(result.getId())
                    .toUri();
            return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
        } catch (Exception e) {
            log.error("Registration failed for username: {}", request.getUsername(), e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Registration failed: " + e.getMessage()));
        }
    }

    /**
     * User login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest req) {
        try {
            ResponseEntity response = userService.authenticateUser(req);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Login failed: " + e.getMessage()));
        }
    }

//    @PostMapping("/google")
//    public ResponseEntity<?> googleAuth(@Valid @RequestBody GoogleAuthRequest req) {
//        try {
//            AuthResponse response = userService.googleAuth(req);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ApiResponse(false, "Google authentication failed: " + e.getMessage()));
//        }
//    }
//
//    /**
//     * Google OAuth callback endpoint
//     */
//    @PostMapping("/google/callback")
//    public ResponseEntity<?> googleAuthCallback(
//            @Valid @RequestBody GoogleAuthRequest request) {
//        log.info("Google OAuth callback request received");
//
//        try {
//            AuthResponse response = userService.googleAuth(request);
//            log.info("Google OAuth callback processed successfully");
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            log.error("Google OAuth callback failed", e);
//            return ResponseEntity.badRequest().body(new ApiResponse(false, "Google OAuth failed: " + e.getMessage()));
//        }
//    }
//
//    /**
//     * Email verification endpoint
//     */
//    @PostMapping("/verify-email")
//    public ResponseEntity<?> verifyEmail(
//            @Valid @RequestBody EmailVerificationRequest request) {
//        log.info("Email verification request received for token: {}", request.getToken());
//
//        try {
//            userService.verifyEmail(request.getToken());
//            log.info("Email verified successfully");
//            return ResponseEntity.ok(new ApiResponse(true, "Email verified successfully"));
//        } catch (Exception e) {
//            log.error("Email verification failed", e);
//            return ResponseEntity.badRequest().body(new ApiResponse(false, "Email verification failed: " + e.getMessage()));
//        }
//    }
//
//    /**
//     * Request password reset endpoint
//     */
//    @PostMapping("/forgot-password")
//    public ResponseEntity<?> requestPasswordReset(
//            @Valid @RequestBody PasswordResetRequest request) {
//        log.info("Password reset request received for email: {}", request.getEmail());
//
//        try {
//            userService.requestPasswordReset(request.getEmail());
//            log.info("Password reset email sent successfully");
//            return ResponseEntity.ok(new ApiResponse(true, "Password reset email sent successfully"));
//        } catch (Exception e) {
//            log.error("Password reset request failed", e);
//            return ResponseEntity.badRequest().body(new ApiResponse(false, "Password reset failed: " + e.getMessage()));
//        }
//    }
//
//    /**
//     * Confirm password reset endpoint
//     */
//    @PostMapping("/reset-password")
//    public ResponseEntity<?> confirmPasswordReset(
//            @Valid @RequestBody PasswordResetConfirmRequest request) {
//        log.info("Password reset confirmation request received");
//
//        try {
//            userService.resetPassword(request.getToken(), request.getNewPassword());
//            log.info("Password reset successfully");
//            return ResponseEntity.ok(new ApiResponse(true, "Password reset successfully"));
//        } catch (Exception e) {
//            log.error("Password reset confirmation failed", e);
//            return ResponseEntity.badRequest().body(new ApiResponse(false, "Password reset failed: " + e.getMessage()));
//        }
//    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        log.info("Logout request received");

        try {
            // TODO: Implement token blacklisting or invalidation
            // For now, just return success response
            log.info("User logged out successfully");
            return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully"));
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Logout failed: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint for authentication service
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        log.debug("Auth service health check");
        return ResponseEntity.ok(new ApiResponse(true, "Authentication service is running"));
    }

    /**
     * Update current user profile (requires authentication)
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateCurrentUserProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        // TODO: Implement updating current user profile
        log.info("Profile update request received");
        return ResponseEntity.ok(new ApiResponse(true, "Profile update endpoint - implementation pending"));
    }
}