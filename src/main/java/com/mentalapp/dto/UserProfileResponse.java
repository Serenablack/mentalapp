package com.mentalapp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean isEmailVerified;
    private String oauthProvider;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}

