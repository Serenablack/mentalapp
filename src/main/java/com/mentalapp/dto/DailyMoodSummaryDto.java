package com.mentalapp.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DailyMoodSummaryDto {
    private LocalDate date;
    private Integer energyLevel;
    private List<String> emotions;
    private int activityCount;
    private int completedActivityCount;
}