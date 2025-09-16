package com.mentalapp.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class SuggestedActivityResponse {
    private Long id;
    private String activityDescription;
    private Boolean isCompleted;
    private Instant createdAt;
}
