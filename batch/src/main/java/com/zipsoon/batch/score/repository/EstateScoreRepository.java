package com.zipsoon.batch.score.repository;

import com.zipsoon.batch.score.mapper.EstateScoreMapper;
import com.zipsoon.batch.score.model.EstateScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EstateScoreRepository {
    private final EstateScoreMapper estateScoreMapper;

    public void saveAll(List<EstateScore> scores) {
        if (!scores.isEmpty()) {
            estateScoreMapper.insertAll(scores);
        }
    }
}