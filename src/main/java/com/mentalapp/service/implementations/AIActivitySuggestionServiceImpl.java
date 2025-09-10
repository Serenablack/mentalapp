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
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIActivitySuggestionServiceImpl implements AIActivitySuggestionService {

    private final RestTemplate restTemplate;
    private final SuggestedActivityRepository suggestedActivityRepository;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Override
    public List<SuggestedActivity> generateSuggestions(MoodEntry moodEntry) {
        log.info("Generating AI suggestions for mood entry: {}", moodEntry.getId());

        try {
            // Validate API configuration
            validateApiConfiguration();

            // Generate prompt for Gemini API
            String prompt = generatePrompt(moodEntry);
            log.debug("Generated prompt for mood entry {}: {}", moodEntry.getId(), prompt);

            // Call Gemini API
            String geminiResponse = callGeminiAPI(prompt);
            log.info("Received response from Gemini API for mood entry: {}", moodEntry.getId());

            // Process Gemini response and create activities
            List<SuggestedActivity> activities = processGeminiResponse(geminiResponse, moodEntry);
            log.info("Successfully generated {} activities from Gemini response", activities.size());

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

    @Override
    public boolean testApiConnection() {
        try {
            validateApiConfiguration();
            String testPrompt = "Say hello in one word.";
            String response = callGeminiAPI(testPrompt);
            return response != null && !response.trim().isEmpty();
        } catch (Exception e) {
            log.error("API connection test failed", e);
            return false;
        }
    }

    private String callGeminiAPI(String prompt) {
        validateApiConfiguration();

        try {
            // Prepare the request body for Gemini API
            Map<String, Object> content = Map.of(
                    "parts", List.of(Map.of("text", prompt))
            );

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(content),
                    "generationConfig", Map.of(
                            "temperature", 0.7,
                            "topK", 1,
                            "topP", 1,
                            "maxOutputTokens", 2048
                    ),
                    "safetySettings", List.of(
                            Map.of("category", "HARM_CATEGORY_HARASSMENT", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                            Map.of("category", "HARM_CATEGORY_HATE_SPEECH", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                            Map.of("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                            Map.of("category", "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold", "BLOCK_MEDIUM_AND_ABOVE")
                    )
            );

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiApiKey);

            // Create HTTP entity
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Build full URL with API key as query parameter (alternative method)
            String fullUrl = geminiApiUrl + "?key=" + geminiApiKey;

            log.debug("Making request to Gemini API: {}", geminiApiUrl);

            // Make the API call
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Successfully called Gemini API");
                return response.getBody();
            } else {
                throw new RuntimeException("Gemini API call failed with status: " + response.getStatusCode());
            }

        } catch (RestClientException e) {
            log.error("Error calling Gemini API", e);
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini API", e);
            throw new RuntimeException("Failed to call Gemini API", e);
        }
    }

    private void validateApiConfiguration() {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty() || "your_gemini_api_key_here".equals(geminiApiKey)) {
            throw new RuntimeException("Gemini API key not configured properly. Please set GEMINI_API_KEY environment variable.");
        }
        if (geminiApiUrl == null || geminiApiUrl.trim().isEmpty()) {
            throw new RuntimeException("Gemini API URL not configured");
        }
    }

    private String generatePrompt(MoodEntry moodEntry) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("As a mental health assistant, analyze the following mood entry and suggest 3 personalized activities ");
        prompt.append("that would be beneficial for the person's mental health and well-being. ");
        prompt.append("Consider their emotions, energy level, environment, and interests. ");
        prompt.append("Each activity should be specific, actionable, and appropriate for their current state.\n\n");

        prompt.append("Mood Entry Details:\n");
        prompt.append("- Location: ").append(moodEntry.getLocation() != null ? moodEntry.getLocation() : "Not specified").append("\n");
        prompt.append("- Comfort Environment: ").append(moodEntry.getComfortEnvironment() != null ? moodEntry.getComfortEnvironment() : "Not specified").append("\n");
        prompt.append("- Description: ").append(moodEntry.getDescription() != null ? moodEntry.getDescription() : "Not provided").append("\n");
        prompt.append("- Energy Level (1-5): ").append(moodEntry.getEnergyLevel() != null ? moodEntry.getEnergyLevel() : "Not specified").append("\n");
        prompt.append("- Passion/Interest: ").append(moodEntry.getPassion() != null ? moodEntry.getPassion() : "Not specified").append("\n");

        Set<Emotion> emotions = moodEntry.getEmotions();
        if (emotions != null && !emotions.isEmpty()) {
            String emotionLabels = emotions.stream()
                                           .map(Emotion::getLabel)
                                           .collect(Collectors.joining(", "));
            prompt.append("- Current Emotions: ").append(emotionLabels).append("\n");
        } else {
            prompt.append("- Current Emotions: None recorded\n");
        }

        prompt.append("\nPlease respond with ONLY a JSON array containing exactly 3 activity objects. ");
        prompt.append("Each object must have these exact fields:\n");
        prompt.append("{\n");
        prompt.append("  \"description\": \"string (detailed description of the activity, 50-150 characters)\",\n");
        prompt.append("  \"type\": \"string (one of: mindfulness, physical, creative, social, self_care, breathing, gratitude)\",\n");
        prompt.append("  \"duration\": number (estimated duration in minutes, between 5-60),\n");
        prompt.append("  \"difficulty\": number (1-5, where 1 is very easy and 5 is very challenging),\n");
        prompt.append("  \"priority\": number (1-5, where 1 is highest priority and 5 is lowest priority)\n");
        prompt.append("}\n\n");
        prompt.append("Respond with ONLY the JSON array, no additional text before or after.");

        return prompt.toString();
    }

    private List<SuggestedActivity> processGeminiResponse(String geminiResponse, MoodEntry moodEntry) {
        try {
            // Parse the Gemini API response
            JsonNode responseNode = objectMapper.readTree(geminiResponse);

            // Extract the text content from the response
            String content = extractContentFromResponse(responseNode);
            log.debug("Extracted content from Gemini response: {}", content);

            // Parse the JSON array of activities
            List<Map<String, Object>> activitiesData = parseActivitiesFromContent(content);

            // Convert to SuggestedActivity objects
            List<SuggestedActivity> activities = activitiesData.stream()
                                                               .map(activityData -> createActivityFromGeminiResponse(activityData, moodEntry))
                                                               .collect(Collectors.toList());

            // Ensure we have exactly 3 activities
            if (activities.isEmpty()) {
                log.warn("No activities parsed from Gemini response, using fallback activities");
                return generateFallbackActivities(moodEntry, 3);
            } else if (activities.size() < 3) {
                log.warn("Gemini API returned only {} activities, adding fallback activities", activities.size());
                activities.addAll(generateFallbackActivities(moodEntry, 3 - activities.size()));
            } else if (activities.size() > 3) {
                log.info("Gemini API returned {} activities, limiting to 3", activities.size());
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
                            return text.asText().trim();
                        }
                    }
                }
            }
            throw new RuntimeException("Could not extract content from Gemini response");
        } catch (Exception e) {
            log.error("Error extracting content from Gemini response: {}", responseNode.toString(), e);
            throw new RuntimeException("Failed to extract content from Gemini response", e);
        }
    }

    private List<Map<String, Object>> parseActivitiesFromContent(String content) {
        try {
            // Clean the content - remove any markdown formatting
            content = content.replace("```json", "").replace("```", "").trim();

            // Try to find JSON array in the response content
            int startBracket = content.indexOf('[');
            int endBracket = content.lastIndexOf(']');

            if (startBracket != -1 && endBracket != -1 && endBracket > startBracket) {
                String jsonArray = content.substring(startBracket, endBracket + 1);
                log.debug("Extracted JSON array: {}", jsonArray);

                List<Map<String, Object>> activities = objectMapper.readValue(jsonArray, new TypeReference<List<Map<String, Object>>>() {});

                if (activities.isEmpty()) {
                    throw new RuntimeException("Parsed activities list is empty");
                }

                return activities;
            } else {
                throw new RuntimeException("No JSON array found in Gemini response content: " + content);
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing activities JSON from Gemini response content: {}", content, e);
            throw new RuntimeException("Failed to parse activities from Gemini response", e);
        }
    }

    private SuggestedActivity createActivityFromGeminiResponse(Map<String, Object> activityData, MoodEntry moodEntry) {
        SuggestedActivity activity = new SuggestedActivity();
        activity.setMoodEntry(moodEntry);

        // Extract data from the Gemini response with validation
        activity.setActivityDescription(getStringValue(activityData, "description", "Beneficial activity for your current mood"));
        activity.setActivityType(getStringValue(activityData, "type", "self_care"));
        activity.setEstimatedDurationMinutes(getIntegerValue(activityData, "duration", 10));
        activity.setDifficultyLevel(Math.max(1, Math.min(5, getIntegerValue(activityData, "difficulty", 2))));
        activity.setPriorityLevel(Math.max(1, Math.min(5, getIntegerValue(activityData, "priority", 3))));
        activity.setIsCompleted(false);

        log.debug("Created activity from Gemini response: {}", activity.getActivityDescription());
        return activity;
    }

    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        if (value != null) {
            String stringValue = value.toString().trim();
            return stringValue.isEmpty() ? defaultValue : stringValue;
        }
        return defaultValue;
    }

    private Integer getIntegerValue(Map<String, Object> data, String key, Integer defaultValue) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt(((String) value).trim());
            } catch (NumberFormatException e) {
                log.warn("Could not parse integer value for key '{}': {}", key, value);
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private List<SuggestedActivity> generateFallbackActivities(MoodEntry moodEntry, int count) {
        List<SuggestedActivity> fallbacks = new ArrayList<>();

        if (count > 0) {
            fallbacks.add(createActivity(moodEntry,
                    "Take 5 deep breaths, focusing on inhaling calm energy and exhaling tension. This can help center your mind and reduce stress.",
                    "breathing", 5, 1, 1));
        }
        if (count > 1) {
            fallbacks.add(createActivity(moodEntry,
                    "Write down three things you're grateful for today, no matter how small. This practice can shift your perspective positively.",
                    "gratitude", 10, 2, 2));
        }
        if (count > 2) {
            fallbacks.add(createActivity(moodEntry,
                    "Take a 10-minute walk or do gentle stretching. Physical movement can boost your energy and improve your mood naturally.",
                    "physical", 10, 2, 3));
        }

        // Add more fallback activities if needed
        if (count > 3) {
            fallbacks.add(createActivity(moodEntry,
                    "Listen to your favorite calming music or nature sounds for 15 minutes while relaxing in a comfortable position.",
                    "self_care", 15, 1, 4));
        }

        return fallbacks.subList(0, Math.min(count, fallbacks.size()));
    }

    private List<SuggestedActivity> generateAndSaveFallbackActivities(MoodEntry moodEntry, int count) {
        List<SuggestedActivity> fallbacks = generateFallbackActivities(moodEntry, count);
        return fallbacks.stream()
                        .map(suggestedActivityRepository::save)
                        .collect(Collectors.toList());
    }

    private SuggestedActivity createActivity(MoodEntry moodEntry, String description,
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