package com.mentalapp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class EmailVerificationRequest {
    @NotBlank(message = "Verification token is required")
    private String token;
}

