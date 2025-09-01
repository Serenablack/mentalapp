package com.mentalapp.service.implementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentalapp.model.Emotion;
import com.mentalapp.model.MoodEntry;
import com.mentalapp.model.SuggestedActivity;
import com.mentalapp.repository.SuggestedActivityRepository;
import com.mentalapp.service.AIActivitySuggestionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIActivitySuggestionServiceImpl implements AIActivitySuggestionService {

    private final RestTemplate restTemplate;
    private final SuggestedActivityRepository suggestedActivityRepository;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String geminiApiUrl;

    @Override
    public List<SuggestedActivity> generateSuggestions(MoodEntry moodEntry) {
        log.info("Generating AI suggestions for mood entry: {}", moodEntry.getId());

        try {
            // Generate prompt for Gemini API
            String prompt = generatePrompt(moodEntry);
            log.debug("Generated prompt for mood entry {}: {}", moodEntry.getId(), prompt);

            // Call Gemini API
            String geminiResponse = callGeminiAPI(prompt);
            log.info("Received response from Gemini API for mood entry: {}", moodEntry.getId());

            // Process Gemini response and create activities
            List<SuggestedActivity> activities = processGeminiResponse(geminiResponse, moodEntry);

            // Save all activities to database
            List<SuggestedActivity> savedActivities = activities.stream()
                    .map(suggestedActivityRepository::save)
                    .collect(Collectors.toList());

            log.info("Successfully generated and saved {} activities for mood entry: {}",
                    savedActivities.size(), moodEntry.getId());
            return savedActivities;

        } catch (Exception e) {
            log.error("Error generating AI suggestions for mood entry: {}", moodEntry.getId(), e);
            // Return fallback activities instead of throwing exception
            return generateAndSaveFallbackActivities(moodEntry, 3);
        }
    }

    private String callGeminiAPI(String prompt) {
        validateApiConfiguration();

        try {
            // Prepare the request body for Gemini API
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", prompt)))));

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiApiKey);

            // Create HTTP entity
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Make the API call
            ResponseEntity<String> response = restTemplate.postForEntity(geminiApiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Successfully called Gemini API");
                return response.getBody();
            } else {
                throw new RuntimeException("Gemini API call failed with status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            throw new RuntimeException("Failed to call Gemini API", e);
        }
    }

    private void validateApiConfiguration() {
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            throw new RuntimeException("Gemini API key not configured");
        }
    }

    private String generatePrompt(MoodEntry moodEntry) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Based on the following mood entry, suggest 3 personalized activities that would be beneficial for mental health and well-being. ");
        prompt.append("Consider the user's emotions, energy level, environment, and passion. ");
        prompt.append("Each activity should be specific, actionable, and appropriate for their current state.\\n\\n");

        prompt.append("Mood Entry Details:\\n");
        prompt.append("- Entry Date: ").append(moodEntry.getEntryDate().toString()).append("\\n");
        prompt.append("- Location: ").append(moodEntry.getLocation() != null ? moodEntry.getLocation() : "N/A").append("\\n");
        prompt.append("- Comfort Environment: ").append(moodEntry.getComfortEnvironment() != null ? moodEntry.getComfortEnvironment() : "N/A").append("\\n");
        prompt.append("- Description: ").append(moodEntry.getDescription() != null ? moodEntry.getDescription() : "N/A").append("\\n");
        prompt.append("- Energy Level (1-5): ").append(moodEntry.getEnergyLevel()).append("\\n");
        prompt.append("- Passion: ").append(moodEntry.getPassion() != null ? moodEntry.getPassion() : "N/A").append("\\n");

        Set<Emotion> emotions = moodEntry.getEmotions();
        if (emotions != null && !emotions.isEmpty()) {
            String emotionLabels = emotions.stream()
                    .map(Emotion::getLabel)
                    .collect(Collectors.joining(", "));
            prompt.append("- Emotions: ").append(emotionLabels).append("\\n");
        } else {
            prompt.append("- Emotions: None recorded\\n");
        }

        prompt.append("\\nProvide the suggestions as a JSON array of objects, where each object has the following fields:\\n");
        prompt.append("{\\n");
        prompt.append("  \"description\": \"string (detailed description of the activity)\",\\n");
        prompt.append("  \"type\": \"string (e.g., 'mindfulness', 'physical', 'creative', 'social', 'self_care')\",\\n");
        prompt.append("  \"duration\": \"integer (estimated duration in minutes)\",\\n");
        prompt.append("  \"difficulty\": \"integer (1-5, 1 being easy, 5 being hard)\",\\n");
        prompt.append("  \"priority\": \"integer (1-5, 1 being high priority, 5 being low priority)\"\\n");
        prompt.append("}\\n");
        prompt.append("Ensure the output is a valid JSON array, without any additional text or markdown outside the JSON block.");

        return prompt.toString();
    }

    private List<SuggestedActivity> processGeminiResponse(String geminiResponse, MoodEntry moodEntry) {
        try {
            // Parse the Gemini API response
            JsonNode responseNode = objectMapper.readTree(geminiResponse);

            // Extract the text content from the response
            String content = extractContentFromResponse(responseNode);
            log.info("Extracted content from Gemini response: {}", content);

            // Parse the JSON array of activities
            List<Map<String, Object>> activitiesData = parseActivitiesFromContent(content);

            // Convert to SuggestedActivity objects
            List<SuggestedActivity> activities = activitiesData.stream()
                .map(activityData -> createActivityFromGeminiResponse(activityData, moodEntry))
                .collect(Collectors.toList());

            // Ensure we have exactly 3 activities
            if (activities.size() < 3) {
                log.warn("Gemini API returned only {} activities, generating fallback activities", activities.size());
                activities.addAll(generateFallbackActivities(moodEntry, 3 - activities.size()));
            } else if (activities.size() > 3) {
                activities = activities.subList(0, 3);
            }

            return activities;

        } catch (Exception e) {
            log.error("Error processing Gemini response, using fallback activities", e);
            return generateFallbackActivities(moodEntry, 3);
        }
    }

    private String extractContentFromResponse(JsonNode responseNode) {
        try {
            // Navigate through the response structure to extract the text content
            JsonNode candidates = responseNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        JsonNode firstPart = parts.get(0);
                        JsonNode text = firstPart.get("text");
                        if (text != null) {
                            return text.asText();
                        }
                    }
                }
            }
            throw new RuntimeException("Could not extract content from Gemini response");
        } catch (Exception e) {
            log.error("Error extracting content from Gemini response", e);
            throw new RuntimeException("Failed to extract content from Gemini response", e);
        }
    }

    private List<Map<String, Object>> parseActivitiesFromContent(String content) {
        try {
            // Try to find JSON array in the response content
            int startBracket = content.indexOf('[');
            int endBracket = content.lastIndexOf(']');

            if (startBracket != -1 && endBracket != -1 && endBracket > startBracket) {
                String jsonArray = content.substring(startBracket, endBracket + 1);
                return objectMapper.readValue(jsonArray, new TypeReference<List<Map<String, Object>>>() {});
            } else {
                throw new RuntimeException("No JSON array found in Gemini response");
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing activities JSON from Gemini response", e);
            throw new RuntimeException("Failed to parse activities from Gemini response", e);
        }
    }

    private SuggestedActivity createActivityFromGeminiResponse(Map<String, Object> activityData, MoodEntry moodEntry) {
        SuggestedActivity activity = new SuggestedActivity();
        activity.setMoodEntry(moodEntry);

        // Extract data from the Gemini response
        activity.setActivityDescription(getStringValue(activityData, "description", "Activity description"));
        activity.setActivityType(getStringValue(activityData, "type", "self_care"));
        activity.setEstimatedDurationMinutes(getIntegerValue(activityData, "duration", 5));
        activity.setDifficultyLevel(getIntegerValue(activityData, "difficulty", 2));
        activity.setPriorityLevel(getIntegerValue(activityData, "priority", 3));
        activity.setIsCompleted(false);

        return activity;
    }

    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private Integer getIntegerValue(Map<String, Object> data, String key, Integer defaultValue) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private List<SuggestedActivity> generateFallbackActivities(MoodEntry moodEntry, int count) {
        List<SuggestedActivity> fallbacks = new ArrayList<>();
        if (count > 0) {
            fallbacks.add(createActivity(moodEntry, "Mindful Breathing Exercise",
                "Take 5 deep breaths, focusing on the sensation of breathing in calm and breathing out tension. This simple exercise can help center your mind and reduce stress.",
                "breathing", 3, 1, 3));
        }
        if (count > 1) {
            fallbacks.add(createActivity(moodEntry, "Gratitude Reflection",
                "Write down three things you're grateful for today, no matter how small. This practice can shift your perspective and improve your mood.",
                "gratitude", 5, 2, 4));
        }
        if (count > 2) {
            fallbacks.add(createActivity(moodEntry, "Gentle Movement",
                "Take a 5-minute walk or do some gentle stretching. Physical movement, even briefly, can boost your energy and improve your mood.",
                "physical", 7, 2, 3));
        }
        return fallbacks;
    }

    private List<SuggestedActivity> generateAndSaveFallbackActivities(MoodEntry moodEntry, int count) {
        List<SuggestedActivity> fallbacks = generateFallbackActivities(moodEntry, count);
        return fallbacks.stream()
                .map(suggestedActivityRepository::save)
                .collect(Collectors.toList());
    }

    private SuggestedActivity createActivity(MoodEntry moodEntry, String title, String description,
            String activityType, Integer duration, Integer difficulty, Integer priority) {
        SuggestedActivity activity = new SuggestedActivity();
        activity.setMoodEntry(moodEntry);
        activity.setActivityDescription(description);
        activity.setActivityType(activityType);
        activity.setEstimatedDurationMinutes(duration);
        activity.setDifficultyLevel(difficulty);
        activity.setPriorityLevel(priority);
        activity.setIsCompleted(false);
        return activity;
    }
}

