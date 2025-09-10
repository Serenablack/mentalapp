package com.mentalapp.security.oauth2;

import com.mentalapp.repository.UserRepository;
import com.mentalapp.security.oauth2.user.GoogleUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        GoogleUserInfo googleUser = new GoogleUserInfo(oAuth2User.getAttributes());

        if (!StringUtils.hasText(googleUser.getEmail())) {
            throw new RuntimeException("Email not found from Google");
        }

//        User user = userRepository.findByGoogleId(googleUser.getId())
//                .orElseGet(() -> userRepository.findByEmail(googleUser.getEmail())
//                        .map(existingUser -> {
//                            // Link existing account with Google
//                            existingUser.setExternalId(googleUser.getId());
//                            existingUser.setIsEmailVerified(true);
//                            return userRepository.save(existingUser);
//                        })
//                        .orElseGet(() -> createGoogleUser(googleUser)));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                oAuth2User.getAttributes(),
                "email");
    }
//
//    private User createGoogleUser(GoogleUserInfo googleUser) {
//        String username = generateUniqueUsername(googleUser.getName());
//
//        User user = User.builder()
//                        .username(username)
//                        .email(googleUser.getEmail())
//                        .googleId(googleUser.getId())
//                        .isEmailVerified(true)
//                        .build();
//
//        return userRepository.save(user);
//    }

    private String generateUniqueUsername(String name) {
        String baseUsername = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter++;
        }

        return username;
    }
}



