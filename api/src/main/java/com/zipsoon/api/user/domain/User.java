package com.zipsoon.api.user.domain.user;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.AuthProvider;
import java.time.LocalDateTime;

@Getter
@Builder
public class User {
    private Long id;
    private String email;
    private String password;
    private String name;
    private String imageUrl;
    private boolean emailVerified;
    private Role role;
    private AuthProvider provider;
    private String providerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User withUpdatedProfile(String name, String imageUrl) {
        return User.builder()
            .id(this.id)
            .email(this.email)
            .password(this.password)
            .name(name)
            .imageUrl(imageUrl)
            .role(this.role)
            .provider(this.provider)
            .providerId(this.providerId)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .build();
    }

    public static User createNewUser(String email, String password, String name, PasswordEncoder encoder) {
        return User.builder()
                .email(email)
                .password(encoder.encode(password))
                .name(name)
                .role(Role.USER)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void updateProfile(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isValidPassword(PasswordEncoder encoder, String rawPassword) {
        return encoder.matches(rawPassword, this.password);
    }

    public boolean hasRole(Role role) {
        return this.role == role;
    }

}