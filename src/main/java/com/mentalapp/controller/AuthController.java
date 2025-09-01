package com.mentalapp.controller;

import com.mentalapp.dto.UserRegistrationRequest;
import com.mentalapp.dto.AuthResponse;
import com.mentalapp.dto.UserLoginRequest;
import com.mentalapp.dto.GoogleAuthRequest;
import com.mentalapp.dto.EmailVerificationRequest;
import com.mentalapp.dto.PasswordResetRequest;
import com.mentalapp.dto.PasswordResetConfirmRequest;
import com.mentalapp.dto.UserUpdateRequest;
import com.mentalapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<AuthResponse> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {
        log.info("Registration request received for username: {}", request.getUsername());

        try {

            log.info("User registered successfully: {}", request.getUsername());

            return ResponseEntity.ok(new AuthResponse());
        } catch (Exception e) {
            log.error("Registration failed for username: {}", request.getUsername(), e);

            return ResponseEntity.badRequest()
                    .body(new AuthResponse("Registration failed: " + e.getMessage()));
        }
    }

    /**
     * User login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody UserLoginRequest request) {
        log.info("Login request received for user: {}", request.getUsernameOrEmail());

        try {
            AuthResponse response = userService.authenticateUser(request);
            log.info("User logged in successfully: {}", request.getUsernameOrEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getUsernameOrEmail(), e);

            return ResponseEntity.badRequest().body(new AuthResponse("Login failed: " + e.getMessage()));
        }
    }

    /**
     * Google OAuth callback endpoint
     */
    @PostMapping("/google/callback")
    public ResponseEntity<AuthResponse> googleAuthCallback(
            @Valid @RequestBody GoogleAuthRequest request) {
        log.info("Google OAuth callback request received");

        try {
            // TODO: Implement Google OAuth verification and user creation/login
            // For now, return a placeholder response
            log.info("Google OAuth callback processed");

            AuthResponse response = new AuthResponse();
            response.setMessage("Google OAuth callback received. Implementation pending.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Google OAuth callback failed", e);

            AuthResponse response = new AuthResponse();
            response.setMessage("Google OAuth failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Email verification endpoint
     */
    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(
            @Valid @RequestBody EmailVerificationRequest request) {
        log.info("Email verification request received for token: {}", request.getToken());

        try {
            userService.verifyEmail(request.getToken());
            log.info("Email verified successfully");

            AuthResponse response = new AuthResponse();
            response.setMessage("Email verified successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Email verification failed", e);

            AuthResponse response = new AuthResponse();
            response.setMessage("Email verification failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Request password reset endpoint
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        log.info("Password reset request received for email: {}", request.getEmail());

        try {
            userService.requestPasswordReset(request.getEmail());
            log.info("Password reset email sent successfully");

            AuthResponse response = new AuthResponse();
            response.setMessage("Password reset email sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Password reset request failed", e);

            AuthResponse response = new AuthResponse();
            response.setMessage("Password reset failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Confirm password reset endpoint
     */
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request) {
        log.info("Password reset confirmation request received");

        try {
            userService.resetPassword(request.getToken(), request.getNewPassword());
            log.info("Password reset successfully");

            AuthResponse response = new AuthResponse();
            response.setMessage("Password reset successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Password reset confirmation failed", e);

            AuthResponse response = new AuthResponse();
            response.setMessage("Password reset failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        log.info("Logout request received");

        try {
            // TODO: Implement token blacklisting or invalidation
            // For now, just return success response
            log.info("User logged out successfully");

            AuthResponse response = new AuthResponse();
            response.setMessage("Logged out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Logout failed", e);

            AuthResponse response = new AuthResponse();
            response.setMessage("Logout failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Health check endpoint for authentication service
     */
    @GetMapping("/health")
    public ResponseEntity<AuthResponse> healthCheck() {
        log.debug("Auth service health check");

        AuthResponse response = new AuthResponse();
        response.setMessage("Authentication service is running");
        return ResponseEntity.ok(response);
    }

    /**
     * Update current user profile (requires authentication)
     */
    @PutMapping("/profile")
    public ResponseEntity<AuthResponse> updateCurrentUserProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        // TODO: Implement updating current user profile
        log.info("Profile update request received");

        AuthResponse response = new AuthResponse("Profile update endpoint - implementation pending");
        return ResponseEntity.ok(response);
    }
}
