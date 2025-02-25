package com.zipsoon.batch.score.mapper;

import com.zipsoon.batch.score.model.EstateScore;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ScoreMapper {
    void insertAll(List<EstateScore> rawScores);
}