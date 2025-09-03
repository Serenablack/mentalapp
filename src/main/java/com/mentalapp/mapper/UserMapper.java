package com.mentalapp.mapper;

import com.mentalapp.model.User;
import com.mentalapp.dto.UserProfileResponse;
import com.mentalapp.dto.UserRegistrationRequest;
import com.mentalapp.dto.UserUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRegistrationRequest request) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());
//        user.setFirstName(request.getFirstName());
//        user.setLastName(request.getLastName());
        // Note: other fields will be set by service
        return user;
    }

    public UserProfileResponse toProfileResponse(User user) {
        if (user == null) {
            return null;
        }

        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
//        response.setFirstName(user.getFirstName());
//        response.setLastName(user.getLastName());
        response.setIsEmailVerified(user.getIsEmailVerified());
        // Note: oauthProvider field doesn't exist in User model
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginAt(user.getLastLoginAt());
        return response;
    }

    public void updateEntity(User entity, UserUpdateRequest request) {
        if (entity == null || request == null) {
            return;
        }

//        if (request.getFirstName() != null) {
//            entity.setFirstName(request.getFirstName());
//        }
//        if (request.getLastName() != null) {
//            entity.setLastName(request.getLastName());
//        }
        if (request.getEmail() != null) {
            entity.setEmail(request.getEmail());
        }
        if (request.getUsername() != null) {
            entity.setUsername(request.getUsername());
        }
    }
}