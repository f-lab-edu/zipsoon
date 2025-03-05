package com.zipsoon.api.infrastructure.repository.user;

import com.zipsoon.api.domain.user.UserDisabledScoreType;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 사용자별 비활성화된 점수 유형 저장소 인터페이스
 */
@Repository
public interface UserDisabledScoreTypeRepository {
    
    /**
     * 사용자 ID로 비활성화된 모든 점수 유형 ID를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 비활성화된 점수 유형 ID 목록
     */
    List<Integer> findDisabledScoreTypeIdsByUserId(Long userId);
    
    /**
     * 점수 유형을 비활성화합니다.
     * 
     * @param userDisabledScoreType 비활성화할 점수 유형 정보
     */
    void insert(UserDisabledScoreType userDisabledScoreType);
    
    /**
     * 비활성화된 점수 유형을 다시 활성화합니다.
     * 
     * @param userId 사용자 ID
     * @param scoreTypeId 점수 유형 ID
     */
    void delete(Long userId, Integer scoreTypeId);
}