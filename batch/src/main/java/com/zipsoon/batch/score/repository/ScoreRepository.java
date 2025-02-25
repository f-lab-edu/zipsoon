package com.zipsoon.batch.score.repository;

import com.zipsoon.batch.score.mapper.ScoreMapper;
import com.zipsoon.batch.score.model.EstateScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ScoreRepository {
    private final ScoreMapper scoreMapper;

    public void saveAll(List<EstateScore> rawScores) {
        scoreMapper.insertAll(rawScores);
    }

}