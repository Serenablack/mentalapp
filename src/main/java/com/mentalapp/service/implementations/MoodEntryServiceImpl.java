package com.mentalapp.service.implementations;

import com.mentalapp.exception.ResourceNotFoundException;
import com.mentalapp.model.MoodEntry;
import com.mentalapp.model.User;
import com.mentalapp.dto.MoodEntryCreateRequest;
import com.mentalapp.dto.MoodEntryResponse;
import com.mentalapp.dto.MoodEntryUpdateRequest;
import com.mentalapp.dto.DailyMoodSummaryDto;
import com.mentalapp.mapper.MoodEntryMapper;
import com.mentalapp.repository.MoodEntryRepository;
import com.mentalapp.service.AIActivitySuggestionService;
import com.mentalapp.service.MoodEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoodEntryServiceImpl implements MoodEntryService {

    private final MoodEntryRepository moodEntryRepository;
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
            moodEntry.setSuggestedActivities(new HashSet<>(aiActivitySuggestionService.generateSuggestions(moodEntry)));
            moodEntry = moodEntryRepository.save(moodEntry);
            log.info("Generated {} AI suggestions for mood entry: {}",
                    moodEntry.getSuggestedActivities().size(), moodEntry.getId());
        } catch (Exception e) {
            log.error("Failed to generate AI suggestions for mood entry: {}", moodEntry.getId(), e);
            // Continue without AI suggestions if they fail
        }

        return moodEntryMapper.toResponse(moodEntry);
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
    public List<MoodEntryResponse> getMoodEntriesByDate(User user, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return moodEntryRepository.findByUserIdAndDate(user.getId(), startOfDay, endOfDay)
                .stream()
                .map(moodEntryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MoodEntryResponse getTodaysMoodEntry(User user) {
        LocalDateTime today = LocalDateTime.now();
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
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        return moodEntryRepository.findByUserIdAndDateRange(user.getId(), startDateTime, endDateTime)
                .stream()
                .map(moodEntryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DailyMoodSummaryDto getDailyMoodSummary(User user, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

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

        if (!moodEntry.isFromToday()) {
            throw new IllegalStateException("Cannot update mood entries from previous days");
        }

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

        if (!moodEntry.isFromToday()) {
            throw new IllegalStateException("Cannot delete mood entries from previous days");
        }

        moodEntryRepository.delete(moodEntry);
        log.info("Deleted mood entry: {} for user: {}", id, user.getEmail());
    }
}