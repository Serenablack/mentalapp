package com.mentalapp.service;

import com.mentalapp.dto.AuthResponse;
import com.mentalapp.dto.LoginRequest;
import com.mentalapp.dto.RegisterRequest;
import com.mentalapp.dto.GoogleAuthRequest;
import com.mentalapp.model.User;

public interface AuthService {
    AuthResponse register(RegisterRequest req);

    AuthResponse login(LoginRequest req);

    AuthResponse googleAuth(GoogleAuthRequest req);

    User currentUser();
}
