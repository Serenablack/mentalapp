package com.mentalapp.service;

import com.mentalapp.model.User;
import com.mentalapp.dto.MoodEntryCreateRequest;
import com.mentalapp.dto.MoodEntryResponse;
import com.mentalapp.dto.MoodEntryUpdateRequest;
import com.mentalapp.dto.DailyMoodSummaryDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MoodEntryService {

    // Core CRUD operations
    MoodEntryResponse createMoodEntry(MoodEntryCreateRequest request, User user);

    MoodEntryResponse getMoodEntryById(Long id, User user);

    List<MoodEntryResponse> getMoodEntriesByDate(User user, LocalDateTime date);

    void updateMoodEntry(Long id, MoodEntryUpdateRequest request, User user);

    void deleteMoodEntry(Long id, User user);

    // Daily dashboard and history
    MoodEntryResponse getTodaysMoodEntry(User user);

    List<MoodEntryResponse> getMoodHistory(User user, LocalDate startDate, LocalDate endDate);

    // Specific data for frontend
    DailyMoodSummaryDto getDailyMoodSummary(User user, LocalDate date);
}