package com.zipsoon.api.infrastructure.repository.estate;

import com.zipsoon.api.interfaces.api.estate.dto.ScoreResponse;
import com.zipsoon.api.infrastructure.mapper.estate.ApiScoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ApiScoreRepository {
    private final ApiScoreMapper mapper;

    /**
     * 매물 ID로 점수 목록을 조회합니다.
     *
     * @param estateId 매물 ID
     * @return 점수 목록
     */
    public List<ScoreResponse> findScoresByEstateId(Long estateId) {
        return mapper.selectScoresByEstateId(estateId);
    }
    
    /**
     * 모든 점수 유형을 조회합니다.
     *
     * @return 점수 유형 목록
     */
    public List<Map<String, Object>> findAllScoreTypes() {
        return mapper.selectAllScoreTypes();
    }
}