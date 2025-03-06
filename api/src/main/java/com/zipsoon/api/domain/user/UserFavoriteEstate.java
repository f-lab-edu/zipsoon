package com.zipsoon.api.domain.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserFavoriteEstate {
    private Long userId;
    private Long estateId;
    private LocalDateTime createdAt;

    public static UserFavoriteEstate create(Long userId, Long estateId) {
        return UserFavoriteEstate.builder()
            .userId(userId)
            .estateId(estateId)
            .createdAt(LocalDateTime.now())
            .build();
    }
}