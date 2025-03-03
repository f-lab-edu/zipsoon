package com.zipsoon.api.interfaces.mapper;

import com.zipsoon.api.interfaces.api.estate.dto.ScoreDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApiScoreMapper {
    List<ScoreDto> findScoresByEstateId(@Param("estateId") Long estateId);
}