package com.zipsoon.batch.infrastructure.repository.score;

import com.zipsoon.batch.infrastructure.mapper.score.BatchScoreMapper;
import com.zipsoon.common.domain.EstateScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ScoreRepository {
    private final BatchScoreMapper batchScoreMapper;

    // 최신 부동산 점수 저장
    public void saveAll(List<EstateScore> scores) {
        batchScoreMapper.insertAll(scores);
    }
    
    // 오래된 점수 스냅샷으로 이동
    public void migrateToScoreSnapshot() {
        batchScoreMapper.migrateToScoreSnapshot();
    }
}