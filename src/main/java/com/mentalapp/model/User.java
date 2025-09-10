package com.mentalapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Email
    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "username", unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(name = "external_id", length = 255)
    private String externalId;

    @Column(name = "auth_provider", length = 50)
    private String authProvider;

    // public String getFullName() {
    // if (firstName != null && lastName != null) {
    // return firstName + " " + lastName;
    // } else if (firstName != null) {
    // return firstName;
    // } else if (lastName != null) {
    // return lastName;
    // }
    // return username;
    // }

    // public boolean isAccountLocked() {
    // return accountLockedUntil != null &&
    // accountLockedUntil.isAfter(LocalDateTime.now());
    // }
    //
    // public void incrementFailedLoginAttempts() {
    // this.failedLoginAttempts++;
    // }
    //
    // public void resetFailedLoginAttempts() {
    // this.failedLoginAttempts = 0;
    // this.accountLockedUntil = null;
    // }
    //
    // public void lockAccount(int minutes) {
    // this.accountLockedUntil = LocalDateTime.now().plusMinutes(minutes);
    // }

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For now, return a default role. In a real application, you'd have a roles
        // table
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
