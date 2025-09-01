package com.mentalapp.service;

import com.mentalapp.model.Emotion;
import com.mentalapp.dto.MoodEntryResponse;

import java.util.List;
import java.util.Optional;

public interface EmotionService {

    // Basic CRUD operations
    List<Emotion> getAllEmotions();

    Optional<Emotion> getEmotionById(Long id);

    Optional<Emotion> getEmotionByKey(String key);

    // Hierarchical emotion operations
    List<Emotion> getRootEmotions();

    List<Emotion> getEmotionsByParentKey(String parentKey);

    List<Emotion> getEmotionTaxonomy();

    // Frontend dropdown support
    List<MoodEntryResponse.EmotionResponse> getEmotionsForDropdown();

    // Create/Update emotions (for admin purposes)
    Emotion createEmotion(String key, String label, String parentKey);

    void deleteEmotion(Long id);
}