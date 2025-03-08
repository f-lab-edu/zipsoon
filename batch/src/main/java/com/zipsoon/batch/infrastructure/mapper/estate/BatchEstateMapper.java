package com.zipsoon.batch.infrastructure.mapper.estate;

import com.zipsoon.common.domain.Estate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BatchEstateMapper {
    void insertAll(@Param("list") List<Estate> estates);
    List<Estate> selectAll();
}