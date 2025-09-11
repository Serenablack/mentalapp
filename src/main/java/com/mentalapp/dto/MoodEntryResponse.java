package com.mentalapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoodEntryResponse {

    private Long id;
    private Long userId;
    private String username;
//    private Instant entryDate;
    private String location;
    private String comfortEnvironment;
    private String description;
    private Integer energyLevel;
    private String passion;
    private Set<EmotionResponse> emotions;
    private Set<SuggestedActivityResponse> suggestedActivities;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isFromToday;



}