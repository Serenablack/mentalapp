package com.mentalapp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class ActivityCompletionRequest {
    @NotNull(message = "Activity ID is required")
    private Long activityId;
}

