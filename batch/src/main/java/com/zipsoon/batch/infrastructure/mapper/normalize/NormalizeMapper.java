package com.zipsoon.batch.infrastructure.mapper.normalize;

import com.zipsoon.batch.domain.score.ScoreType;
import com.zipsoon.common.domain.EstateScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface NormalizeMapper {
    List<ScoreType> selectAllActiveScoreType();

    List<EstateScore> selectScoresById(@Param("scoreTypeId") Long scoreTypeId);

    void updateScoresNormalizedByScoreTypeAndIds(
        @Param("scoreTypeId") Long scoreTypeId,
        @Param("updates") Map<Long, Double> updates
    );
}
