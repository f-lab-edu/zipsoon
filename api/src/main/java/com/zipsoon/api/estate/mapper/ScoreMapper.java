package com.zipsoon.api.estate.mapper;

import com.zipsoon.api.estate.dto.ScoreDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScoreMapper {
    List<ScoreDto> findScoresByEstateId(@Param("estateId") Long estateId);
}