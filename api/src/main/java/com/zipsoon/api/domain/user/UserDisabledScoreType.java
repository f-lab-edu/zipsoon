package com.zipsoon.api.domain.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자별 비활성화된 점수 유형 도메인 모델
 */
@Getter
@Builder
public class UserDisabledScoreType {
    private Long userId;
    private Integer scoreTypeId;
    private LocalDateTime createdAt;
    
    /**
     * 새로운 비활성화 설정을 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param scoreTypeId 점수 유형 ID
     * @return 생성된 비활성화 설정
     */
    public static UserDisabledScoreType of(Long userId, Integer scoreTypeId) {
        return UserDisabledScoreType.builder()
            .userId(userId)
            .scoreTypeId(scoreTypeId)
            .createdAt(LocalDateTime.now())
            .build();
    }
}