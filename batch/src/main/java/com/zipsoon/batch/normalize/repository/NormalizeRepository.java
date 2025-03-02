package com.zipsoon.batch.normalize.repository;

import com.zipsoon.batch.normalize.mapper.NormalizeMapper;
import com.zipsoon.batch.score.model.ScoreType;
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
