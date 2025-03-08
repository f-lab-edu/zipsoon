package com.zipsoon.batch.infrastructure.mapper.score;

import com.zipsoon.common.domain.EstateScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BatchScoreMapper {
    void insertAll(@Param("list") List<EstateScore> scores);
    void insertIntoScoreSnapshot();
}