package com.mentalapp.service;

import com.mentalapp.model.MoodEntry;
import com.mentalapp.model.SuggestedActivity;

import java.util.List;

public interface AIActivitySuggestionService {

    /**
     * Generates three suggested activities for a mood entry and saves them to the
     * database
     *
     * @param moodEntry the mood entry to generate suggestions for
     * @return list of generated and saved suggested activities
     */
    List<SuggestedActivity> generateSuggestions(MoodEntry moodEntry);
}

