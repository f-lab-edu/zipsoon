package com.zipsoon.api.infrastructure.mapper.user;

import com.zipsoon.api.domain.user.UserDisabledScoreType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 사용자별 비활성화된 점수 유형 매퍼
 */
@Mapper
public interface UserDisabledScoreTypeMapper {
    
    /**
     * 사용자 ID로 비활성화된 모든 점수 유형 ID를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 비활성화된 점수 유형 ID 목록
     */
    List<Integer> selectDisabledScoreTypeIdsByUserId(Long userId);
    
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
    void delete(@Param("userId") Long userId, @Param("scoreTypeId") Integer scoreTypeId);
}