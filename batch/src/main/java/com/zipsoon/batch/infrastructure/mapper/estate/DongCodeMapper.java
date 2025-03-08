package com.zipsoon.batch.infrastructure.mapper.estate;

import com.zipsoon.batch.domain.estate.DongCode;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DongCodeMapper {
    List<DongCode> selectAll();
}