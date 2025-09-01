package com.mentalapp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class SuggestedActivityUpdateRequest {
    @NotNull(message = "Completion status is required")
    private Boolean isCompleted;
}
