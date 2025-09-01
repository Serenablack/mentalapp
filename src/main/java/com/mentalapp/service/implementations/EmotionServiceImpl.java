package com.mentalapp.service.implementations;

import com.mentalapp.exception.ResourceNotFoundException;
import com.mentalapp.model.Emotion;

import com.mentalapp.dto.MoodEntryResponse;
import com.mentalapp.mapper.EmotionMapper;
import com.mentalapp.repository.EmotionRepository;
import com.mentalapp.service.EmotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmotionServiceImpl implements EmotionService {

    private final EmotionRepository emotionRepository;
    private final EmotionMapper emotionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Emotion> getAllEmotions() {
        return emotionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Emotion> getEmotionById(Long id) {
        return emotionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Emotion> getEmotionByKey(String key) {
        return emotionRepository.findByKey(key);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Emotion> getRootEmotions() {
        return emotionRepository.findRootEmotions();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Emotion> getEmotionsByParentKey(String parentKey) {
        return emotionRepository.findByParentKey(parentKey);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Emotion> getEmotionTaxonomy() {
        return emotionRepository.findRootEmotionsWithChildren();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoodEntryResponse.EmotionResponse> getEmotionsForDropdown() {
        return emotionRepository.findAll().stream()
                .map(emotionMapper::toMoodEntryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Emotion createEmotion(String key, String label, String parentKey) {
        // Check if emotion with this key already exists
        if (emotionRepository.findByKey(key).isPresent()) {
            throw new IllegalArgumentException("Emotion with key '" + key + "' already exists");
        }

        Emotion emotion = new Emotion();
        emotion.setKey(key);
        emotion.setLabel(label);

        // Validate and set parent if parentKey is provided
        if (parentKey != null && !parentKey.isEmpty()) {
            Emotion parent = emotionRepository.findByKey(parentKey)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Parent emotion not found: " + parentKey));
            emotion.setParent(parent);
        }

        Emotion savedEmotion = emotionRepository.save(emotion);
        log.info("Created new emotion: {} with key: {}", label, key);

        return savedEmotion;
    }

    @Override
    @Transactional
    public void deleteEmotion(Long id) {
        Emotion emotion = emotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emotion not found"));

        // Check if this emotion has children
        List<Emotion> children = emotionRepository.findByParentKey(emotion.getKey());
        if (!children.isEmpty()) {
            throw new IllegalStateException("Cannot delete emotion that has child emotions. " +
                    "Delete child emotions first or reassign them to a different parent.");
        }

        emotionRepository.delete(emotion);
        log.info("Deleted emotion: {} with key: {}", emotion.getLabel(), emotion.getKey());
    }
}
