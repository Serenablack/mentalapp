package com.mentalapp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class GoogleAuthRequest {
    @NotBlank(message = "Google token is required")
    private String token;
}