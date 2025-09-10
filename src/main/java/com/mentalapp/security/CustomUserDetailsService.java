package com.mentalapp.security;

import com.mentalapp.model.User;
import com.mentalapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Loading user by username/email: {}", usernameOrEmail);

        // Determine if input looks like email or username
        User user;
        if (usernameOrEmail.contains("@")) {
            // Looks like email, try email first
            log.debug("Input contains '@', treating as email: {}", usernameOrEmail);
            user = userRepository.findByEmail(usernameOrEmail)
                                 .orElseThrow(() -> {
                                     log.warn("User not found with email: {}", usernameOrEmail);
                                     return new UsernameNotFoundException("User not found: " + usernameOrEmail);
                                 });
        } else {
            // Looks like username, try username first then email as fallback
            log.debug("Input doesn't contain '@', treating as username: {}", usernameOrEmail);
            user = userRepository.findByUsername(usernameOrEmail)
                                 .or(() -> {
                                     log.debug("Not found by username, trying as email: {}", usernameOrEmail);
                                     return userRepository.findByEmail(usernameOrEmail);
                                 })
                                 .orElseThrow(() -> {
                                     log.warn("User not found with username/email: {}", usernameOrEmail);
                                     return new UsernameNotFoundException("User not found: " + usernameOrEmail);
                                 });
        }

        if (!user.isEnabled()) {
            log.warn("User account is disabled: {}", usernameOrEmail);
            throw new UsernameNotFoundException("User account is disabled: " + usernameOrEmail);
        }

        log.debug("User loaded successfully - ID: {}, Username: {}, Email: {}",
                user.getId(), user.getUsername(), user.getEmail());
        return user; // Since User implements UserDetails
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        log.debug("Loading user by ID: {}", id);

        User user = userRepository.findById(id)
                                  .orElseThrow(() -> {
                                      log.warn("User not found with id: {}", id);
                                      return new UsernameNotFoundException("User not found with id: " + id);
                                  });

        if (!user.isEnabled()) {
            log.warn("User account is disabled for ID: {}", id);
            throw new UsernameNotFoundException("User account is disabled for id: " + id);
        }

        log.debug("User loaded by ID successfully - Username: {}, Email: {}",
                user.getUsername(), user.getEmail());
        return user;
    }
}