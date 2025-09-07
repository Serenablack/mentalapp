package com.mentalapp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;

    private Boolean isEmailVerified;
    private LocalDateTime createdAt;
}
