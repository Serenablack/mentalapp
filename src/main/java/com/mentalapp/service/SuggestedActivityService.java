package com.mentalapp.service;

import com.mentalapp.model.User;
import com.mentalapp.dto.SuggestedActivityResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface SuggestedActivityService {

    // Fetch activities
    List<SuggestedActivityResponse> getActivitiesByDate(User user, Instant date);

    List<SuggestedActivityResponse> getTodaysActivities(User user);

    // Update activity status
    void markAsCompleted(Long activityId, User user);

    void markAsIncomplete(Long activityId, User user);

    // Delete activity (only if not completed and not from previous days)
    void deleteActivity(Long activityId, User user);

    // Get activity by ID
    SuggestedActivityResponse getActivityById(Long activityId, User user);
}