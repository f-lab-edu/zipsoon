package com.zipsoon.api.infrastructure.repository.estate;

import com.zipsoon.api.interfaces.api.estate.dto.ScoreDto;
import com.zipsoon.api.interfaces.mapper.ApiScoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ApiScoreRepository {
    private final ApiScoreMapper apiScoreMapper;

    public List<ScoreDto> findScoresByEstateId(Long estateId) {
        return apiScoreMapper.findScoresByEstateId(estateId);
    }
}