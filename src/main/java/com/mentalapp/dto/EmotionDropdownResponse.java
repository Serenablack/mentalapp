package com.mentalapp.dto;

import lombok.Data;
import java.util.List;

@Data
public class EmotionDropdownResponse {
    private Long id;
    private String key;
    private String label;
    private String parentKey;
    private List<EmotionDropdownResponse> children;
}

