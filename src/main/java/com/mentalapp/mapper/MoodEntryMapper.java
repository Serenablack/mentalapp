package com.mentalapp.mapper;

import com.mentalapp.model.Emotion;
import com.mentalapp.model.MoodEntry;
import com.mentalapp.model.SuggestedActivity;
import com.mentalapp.dto.MoodEntryCreateRequest;
import com.mentalapp.dto.MoodEntryResponse;
import com.mentalapp.dto.MoodEntryUpdateRequest;
import com.mentalapp.repository.EmotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MoodEntryMapper {

    @Autowired
    private EmotionRepository emotionRepository;

    public MoodEntry toEntity(MoodEntryCreateRequest request) {
        if (request == null) {
            return null;
        }

        MoodEntry moodEntry = new MoodEntry();
        moodEntry.setEntryDate(request.getEntryDate());
        moodEntry.setLocation(request.getLocation());
        moodEntry.setComfortEnvironment(request.getComfortEnvironment());
        moodEntry.setDescription(request.getDescription());
        moodEntry.setEnergyLevel(request.getEnergyLevel());
        moodEntry.setPassion(request.getPassion());

        // Map emotions by IDs
        if (request.getEmotionIds() != null && !request.getEmotionIds().isEmpty()) {
            Set<Emotion> emotions = request.getEmotionIds().stream()
                    .map(emotionRepository::findById)
                    .filter(opt -> opt.isPresent())
                    .map(opt -> opt.get())
                    .collect(Collectors.toSet());
            moodEntry.setEmotions(emotions);
        }

        return moodEntry;
    }

    public MoodEntryResponse toResponse(MoodEntry moodEntry) {
        if (moodEntry == null) {
            return null;
        }

        MoodEntryResponse response = new MoodEntryResponse();
        response.setId(moodEntry.getId());
        response.setUserId(moodEntry.getUser() != null ? moodEntry.getUser().getId() : null);
        response.setUsername(moodEntry.getUser() != null ? moodEntry.getUser().getUsername() : null);
        response.setEntryDate(moodEntry.getEntryDate());
        response.setLocation(moodEntry.getLocation());
        response.setComfortEnvironment(moodEntry.getComfortEnvironment());
        response.setDescription(moodEntry.getDescription());
        response.setEnergyLevel(moodEntry.getEnergyLevel());
        response.setPassion(moodEntry.getPassion());
        response.setCreatedAt(moodEntry.getCreatedAt());
        response.setUpdatedAt(moodEntry.getUpdatedAt());
        response.setIsFromToday(moodEntry.isFromToday());

        // Map emotions
        if (moodEntry.getEmotions() != null) {
            response.setEmotions(moodEntry.getEmotions().stream()
                    .map(this::mapEmotion)
                    .collect(Collectors.toSet()));
        }

        // Map suggested activities
        if (moodEntry.getSuggestedActivities() != null) {
            response.setSuggestedActivities(moodEntry.getSuggestedActivities().stream()
                    .map(this::mapActivity)
                    .collect(Collectors.toSet()));
        }

        return response;
    }

    public void updateEntity(MoodEntry entity, MoodEntryUpdateRequest request) {
        if (request == null || entity == null) {
            return;
        }

        if (request.getLocation() != null) {
            entity.setLocation(request.getLocation());
        }
        if (request.getComfortEnvironment() != null) {
            entity.setComfortEnvironment(request.getComfortEnvironment());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getEnergyLevel() != null) {
            entity.setEnergyLevel(request.getEnergyLevel());
        }
        if (request.getPassion() != null) {
            entity.setPassion(request.getPassion());
        }

        // Update emotions if provided
        if (request.getEmotionIds() != null) {
            Set<Emotion> emotions = request.getEmotionIds().stream()
                    .map(emotionRepository::findById)
                    .filter(opt -> opt.isPresent())
                    .map(opt -> opt.get())
                    .collect(Collectors.toSet());
            entity.setEmotions(emotions);
        }
    }

    private MoodEntryResponse.EmotionResponse mapEmotion(Emotion emotion) {
        if (emotion == null) {
            return null;
        }

        MoodEntryResponse.EmotionResponse response = new MoodEntryResponse.EmotionResponse();
        response.setId(emotion.getId());
        response.setKey(emotion.getKey());
        response.setLabel(emotion.getLabel());
        response.setParentKey(emotion.getParent() != null ? emotion.getParent().getKey() : null);
        return response;
    }

    private MoodEntryResponse.SuggestedActivityResponse mapActivity(SuggestedActivity activity) {
        if (activity == null) {
            return null;
        }

        MoodEntryResponse.SuggestedActivityResponse response = new MoodEntryResponse.SuggestedActivityResponse();
        response.setId(activity.getId());
        response.setActivityDescription(activity.getActivityDescription());
        response.setActivityType(activity.getActivityType());
        response.setEstimatedDurationMinutes(activity.getEstimatedDurationMinutes());
        response.setDifficultyLevel(activity.getDifficultyLevel());
        response.setPriorityLevel(activity.getPriorityLevel());
        response.setIsCompleted(activity.getIsCompleted());
        response.setCompletedAt(activity.getCompletedAt());
        response.setStatus(activity.getStatus());
        response.setCreatedAt(activity.getCreatedAt());
        return response;
    }
}