package com.zipsoon.batch.score.mapper;

import com.zipsoon.batch.score.model.ScoreType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ScoreTypeMapper {
    List<ScoreType> findAllActive();
    Optional<ScoreType> findByName(String name);
}