package com.zipsoon.api.domain.user;

import com.zipsoon.api.domain.auth.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class User {
    private Long id;
    private String email;
    private boolean emailVerified;
    private String name;
    private String imageUrl;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void updateProfile(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void verifyEmail() {
        this.emailVerified = true;
        this.updatedAt = LocalDateTime.now();
    }
}