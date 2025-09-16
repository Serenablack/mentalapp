package com.mentalapp.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private UserProfileResponse user;
    private boolean success = true;
    private String message;

    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public AuthResponse(String accessToken, UserProfileResponse user) {
        this.accessToken = accessToken;
        this.user = user;
    }

    public AuthResponse(String accessToken, UserProfileResponse user, String message) {
        this.accessToken = accessToken;
        this.user = user;
        this.message = message;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
