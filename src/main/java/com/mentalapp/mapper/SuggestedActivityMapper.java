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
        response.setIsCompleted(activity.getIsCompleted());
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