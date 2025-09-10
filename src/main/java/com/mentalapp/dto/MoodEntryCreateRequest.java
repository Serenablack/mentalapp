package com.mentalapp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class MoodEntryCreateRequest {
//    @NotNull(message = "Entry date is required")
//    private Instant entryDate;

    private String location;

    private String comfortEnvironment;

    private String description;

    @NotNull(message = "Energy level is required")
    @Min(value = 1, message = "Energy level must be between 1 and 5")
    @Max(value = 5, message = "Energy level must be between 1 and 5")
    private Integer energyLevel;

    private String passion;

    @NotNull(message = "At least one emotion must be selected")
    private Set<Long> emotionIds;
}

