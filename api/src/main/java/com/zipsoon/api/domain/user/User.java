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
    private String name;
    private String imageUrl;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}