package com.zipsoon.api.infrastructure.mapper.estate;

import com.zipsoon.api.interfaces.api.estate.dto.ScoreResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ApiScoreMapper {
    /**
     * 매물 ID로 점수 목록을 조회합니다.
     *
     * @param estateId 매물 ID
     * @return 점수 목록
     */
    List<ScoreResponse> selectScoresByEstateId(@Param("estateId") Long estateId);
    
    /**
     * 모든 점수 유형을 조회합니다.
     *
     * @return 점수 유형 목록 (Map: ID, 이름, 설명)
     */
    List<Map<String, Object>> selectAllScoreTypes();
}