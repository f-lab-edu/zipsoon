package com.zipsoon.api.infrastructure.mapper.estate;

import com.zipsoon.api.interfaces.api.estate.dto.ViewportRequest;
import com.zipsoon.common.domain.Estate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ApiEstateMapper {
    List<Estate> selectAllInViewport(
        @Param("viewport") ViewportRequest viewport,
        @Param("limit") int limit,
        @Param("srid") int srid
    );

   Optional<Estate> selectById(@Param("id") Long id);
}