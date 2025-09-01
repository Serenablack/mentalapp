package com.mentalapp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class EmotionCreateRequest {
    @NotBlank(message = "Key is required")
    private String key;

    @NotBlank(message = "Label is required")
    private String label;

    private String parentKey;
}

