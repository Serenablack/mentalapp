package com.mentalapp.mapper;

import com.mentalapp.model.Emotion;
import com.mentalapp.dto.EmotionResponse;
import com.mentalapp.dto.EmotionDropdownResponse;
import com.mentalapp.dto.EmotionCreateRequest;
import com.mentalapp.dto.MoodEntryResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmotionMapper {

    public EmotionResponse toResponse(Emotion emotion) {
        if (emotion == null) {
            return null;
        }

        EmotionResponse response = new EmotionResponse();
        response.setId(emotion.getId());
        response.setKey(emotion.getKey());
        response.setLabel(emotion.getLabel());
        response.setParentKey(emotion.getParent() != null ? emotion.getParent().getKey() : null);
        response.setCreatedAt(emotion.getCreatedAt());
        response.setUpdatedAt(emotion.getUpdatedAt());
        // Note: children will be handled separately in service
        return response;
    }

    public EmotionDropdownResponse toDropdownResponse(Emotion emotion) {
        if (emotion == null) {
            return null;
        }

        EmotionDropdownResponse response = new EmotionDropdownResponse();
        response.setId(emotion.getId());
        response.setKey(emotion.getKey());
        response.setLabel(emotion.getLabel());
        response.setParentKey(emotion.getParent() != null ? emotion.getParent().getKey() : null);
        // Note: children will be handled separately in service
        return response;
    }

    public MoodEntryResponse.EmotionResponse toMoodEntryResponse(Emotion emotion) {
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

    public Emotion toEntity(EmotionCreateRequest request) {
        if (request == null) {
            return null;
        }

        Emotion emotion = new Emotion();
        emotion.setKey(request.getKey());
        emotion.setLabel(request.getLabel());
        // Note: parent and children will be set by service
        return emotion;
    }

    public List<EmotionResponse> toResponseList(List<Emotion> emotions) {
        if (emotions == null) {
            return null;
        }
        return emotions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmotionDropdownResponse> toDropdownResponseList(List<Emotion> emotions) {
        if (emotions == null) {
            return null;
        }
        return emotions.stream()
                .map(this::toDropdownResponse)
                .collect(Collectors.toList());
    }

    public List<MoodEntryResponse.EmotionResponse> toMoodEntryResponseList(List<Emotion> emotions) {
        if (emotions == null) {
            return null;
        }
        return emotions.stream()
                .map(this::toMoodEntryResponse)
                .collect(Collectors.toList());
    }
}