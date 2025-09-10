package com.mentalapp.security.oauth2.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class GoogleUserInfo {
    private Map<String, Object> attributes;

    public String getId() {
        return (String) attributes.get("sub");
    }

    public String getName() {
        return (String) attributes.get("name");
    }

    public String getEmail() {
        return (String) attributes.get("email");
    }

    public String getImageUrl() {
        return (String) attributes.get("picture");
    }}