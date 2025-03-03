package com.zipsoon.batch.infrastructure.repository.normalize;

import com.zipsoon.batch.infrastructure.mapper.normalize.NormalizeMapper;
import com.zipsoon.batch.domain.score.ScoreType;
import com.zipsoon.common.domain.EstateScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class NormalizeRepository {
    private final NormalizeMapper normalizeMapper;

    public List<ScoreType> findAllActiveScoreType() {
        return normalizeMapper.selectAllActiveScoreType();
    }

    public List<EstateScore> findByScoreType(Long scoreTypeId) {
        return normalizeMapper.selectScoresById(scoreTypeId);
    }

    public void updateNormalizedScores(Long scoreTypeId, Map<Long, Double> updates) {
        normalizeMapper.updateScoresNormalizedByScoreTypeAndIds(scoreTypeId, updates);
    }
}
