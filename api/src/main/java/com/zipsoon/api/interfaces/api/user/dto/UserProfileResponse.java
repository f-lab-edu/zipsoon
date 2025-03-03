package com.zipsoon.api.interfaces.api.user.dto;

import com.zipsoon.api.domain.user.User;

import java.time.LocalDateTime;

public record UserProfileResponse(
    Long id,
    String email,
    String name,
    String imageUrl,
    LocalDateTime createdAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getImageUrl(),
            user.getCreatedAt()
        );
    }
}
