package com.mentalapp.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private String message;
    
    public AuthResponse() {}
    
    public AuthResponse(String message) {
        this.message = message;
    }
}
