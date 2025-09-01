package com.mentalapp.mapper;

import com.mentalapp.model.SuggestedActivity;
import com.mentalapp.dto.SuggestedActivityResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SuggestedActivityMapper {

    public SuggestedActivityResponse toResponse(SuggestedActivity activity) {
        if (activity == null) {
            return null;
        }

        SuggestedActivityResponse response = new SuggestedActivityResponse();
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

    public List<SuggestedActivityResponse> toResponseList(List<SuggestedActivity> activities) {
        if (activities == null) {
            return null;
        }
        return activities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}