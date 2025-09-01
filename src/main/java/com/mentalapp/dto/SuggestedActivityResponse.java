package com.mentalapp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SuggestedActivityResponse {
    private Long id;
    private String activityDescription;
    private String activityType;
    private Integer estimatedDurationMinutes;
    private Integer difficultyLevel;
    private Integer priorityLevel;
    private Boolean isCompleted;
    private LocalDateTime completedAt;
    private String status;
    private LocalDateTime createdAt;
}

