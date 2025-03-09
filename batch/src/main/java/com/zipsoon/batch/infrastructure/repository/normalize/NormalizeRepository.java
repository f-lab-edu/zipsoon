package com.zipsoon.batch.infrastructure.repository.normalize;

import com.zipsoon.batch.infrastructure.mapper.normalize.NormalizeMapper;
import com.zipsoon.common.domain.EstateScore;
import com.zipsoon.common.domain.score.ScoreType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class NormalizeRepository {
    private final NormalizeMapper mapper;

    public List<ScoreType> findAllActiveScoreTypes() {
        return mapper.selectAllActiveScoreTypes();
    }

    public List<EstateScore> findByScoreTypeId(Long scoreTypeId) {
        return mapper.selectByScoreTypeId(scoreTypeId);
    }

    public void updateNormalizedScores(Long scoreTypeId, Map<Long, Double> updates) {
        mapper.updateNormalizedScoresByScoreTypeIdAndIds(scoreTypeId, updates);
    }
}
