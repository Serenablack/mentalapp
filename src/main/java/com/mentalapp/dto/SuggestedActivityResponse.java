package com.mentalapp.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class SuggestedActivityResponse {
    private Long id;
    private String activityDescription;
    private String activityType;
    private Integer estimatedDurationMinutes;
    private Integer difficultyLevel;
    private Integer priorityLevel;
    private Boolean isCompleted;
    private Instant completedAt;
    private String status;
    private Instant createdAt;
}
