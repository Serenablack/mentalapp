package com.mentalapp.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EmotionResponse {
    private Long id;
    private String key;
    private String label;
    private String parentKey;
    private List<EmotionResponse> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

