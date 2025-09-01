package com.mentalapp.service;

import com.mentalapp.model.User;
import com.mentalapp.dto.UserRegistrationRequest;
import com.mentalapp.dto.AuthResponse;
import com.mentalapp.dto.UserProfileResponse;
import com.mentalapp.dto.UserUpdateRequest;
import com.mentalapp.dto.UserLoginRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    // Registration and Authentication
    User registerUser(UserRegistrationRequest request);

    User registerOAuthUser(String email, String name, String provider);

    AuthResponse authenticateUser(UserLoginRequest request);

    // User Profile Management
    UserProfileResponse getUserProfile(Long userId);

    UserProfileResponse updateUserProfile(Long userId, UserUpdateRequest request);

    // Password Management
    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

    // Email Verification
    void verifyEmail(String token);
}