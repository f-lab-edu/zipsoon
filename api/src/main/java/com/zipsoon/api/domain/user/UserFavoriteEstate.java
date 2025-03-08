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

    /**
     * 사용자가 찜한 매물 정보를 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param estateId 매물 ID
     * @return 생성된 찜 정보
     */
    public static UserFavoriteEstate of(Long userId, Long estateId) {
        return UserFavoriteEstate.builder()
            .userId(userId)
            .estateId(estateId)
            .createdAt(LocalDateTime.now())
            .build();
    }
}