package com.mentalapp.service.implementations;

import com.mentalapp.exception.ResourceNotFoundException;
import com.mentalapp.model.MoodEntry;
import com.mentalapp.model.SuggestedActivity;
import com.mentalapp.model.User;
import com.mentalapp.dto.MoodEntryCreateRequest;
import com.mentalapp.dto.MoodEntryResponse;
import com.mentalapp.dto.MoodEntryUpdateRequest;
import com.mentalapp.dto.DailyMoodSummaryDto;
import com.mentalapp.mapper.MoodEntryMapper;
import com.mentalapp.repository.MoodEntryRepository;
import com.mentalapp.repository.SuggestedActivityRepository;
import com.mentalapp.service.AIActivitySuggestionService;
import com.mentalapp.service.MoodEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoodEntryServiceImpl implements MoodEntryService {

    private final MoodEntryRepository moodEntryRepository;
    private final SuggestedActivityRepository suggestedActivityRepository;
    private final MoodEntryMapper moodEntryMapper;
    private final AIActivitySuggestionService aiActivitySuggestionService;

    @Override
    @Transactional
    public MoodEntryResponse createMoodEntry(MoodEntryCreateRequest request, User user) {
        MoodEntry moodEntry = moodEntryMapper.toEntity(request);
        moodEntry.setUser(user);

        // Save the mood entry first
        moodEntry = moodEntryRepository.save(moodEntry);
        log.info("Created mood entry for user: {} with id: {}", user.getEmail(), moodEntry.getId());

        // Generate and save AI suggestions
        try {
            aiActivitySuggestionService.generateSuggestions(moodEntry);
            log.info("Generated AI suggestions for mood entry: {}", moodEntry.getId());
        } catch (Exception e) {
            log.error("Failed to generate AI suggestions for mood entry: {}", moodEntry.getId(), e);
            // Continue without AI suggestions if they fail
        }

        // Create response and manually fetch suggested activities via repository
        MoodEntryResponse response = moodEntryMapper.toResponse(moodEntry);

        // Fetch suggested activities for this mood entry directly from repository
        List<SuggestedActivity> activities = suggestedActivityRepository.findByMoodEntryId(moodEntry.getId());
        if (!activities.isEmpty()) {
            Set<MoodEntryResponse.SuggestedActivityResponse> activityResponses = activities.stream()
                    .map(activity -> {
                        MoodEntryResponse.SuggestedActivityResponse activityResponse = new MoodEntryResponse.SuggestedActivityResponse();
                        activityResponse.setId(activity.getId());
                        activityResponse.setActivityDescription(activity.getActivityDescription());
                        activityResponse.setIsCompleted(activity.getIsCompleted());
                        activityResponse.setCreatedAt(activity.getCreatedAt());
                        return activityResponse;
                    })
                    .collect(Collectors.toSet());
            response.setSuggestedActivities(activityResponses);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public MoodEntryResponse getMoodEntryById(Long id, User user) {
        MoodEntry moodEntry = moodEntryRepository.findById(id)
                .filter(entry -> entry.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Mood entry not found"));

        return moodEntryMapper.toResponse(moodEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoodEntryResponse> getMoodEntriesByDate(User user, Instant date) {
        // Convert Instant to day boundaries in UTC
        Instant startOfDay = date.atOffset(ZoneOffset.UTC).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endOfDay = startOfDay.atOffset(ZoneOffset.UTC).toLocalDate().plusDays(1).atStartOfDay()
                .toInstant(ZoneOffset.UTC);

        return moodEntryRepository.findByUserIdAndDate(user.getId(), startOfDay, endOfDay)
                .stream()
                .map(moodEntryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MoodEntryResponse getTodaysMoodEntry(User user) {
        Instant today = Instant.now();
        List<MoodEntryResponse> todaysEntries = getMoodEntriesByDate(user, today);

        if (todaysEntries.isEmpty()) {
            return null; // No mood entry for today
        }

        // Return the most recent entry if multiple exist
        return todaysEntries.get(todaysEntries.size() - 1);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoodEntryResponse> getMoodHistory(User user, LocalDate startDate, LocalDate endDate) {
        // Convert LocalDate to Instant boundaries in UTC
        Instant startDateTime = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endDateTime = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        return moodEntryRepository.findByUserIdAndDateRange(user.getId(), startDateTime, endDateTime)
                .stream()
                .map(moodEntryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DailyMoodSummaryDto getDailyMoodSummary(User user, LocalDate date) {
        // Convert LocalDate to Instant boundaries in UTC
        Instant startOfDay = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endOfDay = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<MoodEntry> entries = moodEntryRepository.findByUserIdAndDate(user.getId(), startOfDay, endOfDay);

        if (entries.isEmpty()) {
            return null;
        }

        // Get the most recent entry for the day
        MoodEntry latestEntry = entries.get(entries.size() - 1);

        DailyMoodSummaryDto summary = new DailyMoodSummaryDto();
        summary.setDate(date);
        summary.setEnergyLevel(latestEntry.getEnergyLevel());
        summary.setEmotions(latestEntry.getEmotions().stream()
                .map(emotion -> emotion.getLabel())
                .collect(Collectors.toList()));
        summary.setActivityCount(latestEntry.getSuggestedActivities().size());
        summary.setCompletedActivityCount(
                (int) latestEntry.getSuggestedActivities().stream()
                        .mapToLong(activity -> activity.getIsCompleted() ? 1L : 0L)
                        .sum());

        return summary;
    }

    @Override
    @Transactional
    public void updateMoodEntry(Long id, MoodEntryUpdateRequest request, User user) {
        MoodEntry moodEntry = moodEntryRepository.findById(id)
                .filter(entry -> entry.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Mood entry not found"));

        // if (!moodEntry.isFromToday()) {
        // throw new IllegalStateException("Cannot update mood entries from previous
        // days");
        // }

        moodEntryMapper.updateEntity(moodEntry, request);
        moodEntryRepository.save(moodEntry);

        log.info("Updated mood entry: {} for user: {}", id, user.getEmail());
    }

    @Override
    @Transactional
    public void deleteMoodEntry(Long id, User user) {
        MoodEntry moodEntry = moodEntryRepository.findById(id)
                .filter(entry -> entry.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Mood entry not found"));

        // if (!moodEntry.isFromToday()) {
        // throw new IllegalStateException("Cannot delete mood entries from previous
        // days");
        // }

        moodEntryRepository.delete(moodEntry);
        log.info("Deleted mood entry: {} for user: {}", id, user.getEmail());
    }
}