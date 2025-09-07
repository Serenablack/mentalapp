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
        log.debug("Loading user by username: {}", usernameOrEmail);

        // Try to find user by email first, then by username if not found
        User user = userRepository.findByEmail(usernameOrEmail)
                                  .orElseThrow(() -> {
                                      log.warn("User not found with username/email: {}", usernameOrEmail);
                                      return new UsernameNotFoundException("User not found: " + usernameOrEmail);
                                  });



        if (!user.isEnabled()) {
            log.warn("User account is disabled: {}", usernameOrEmail);
            throw new UsernameNotFoundException("User account is disabled: " + usernameOrEmail);
        }

        log.debug("User loaded successfully: {}", usernameOrEmail);
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

        return user;
    }
}