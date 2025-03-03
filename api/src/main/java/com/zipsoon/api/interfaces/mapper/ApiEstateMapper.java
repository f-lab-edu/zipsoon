package com.zipsoon.api.interfaces.mapper;

import com.zipsoon.api.interfaces.api.estate.dto.ViewportRequest;
import com.zipsoon.common.domain.Estate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ApiEstateMapper {
    List<Estate> findAllInViewport(
        @Param("viewport") ViewportRequest viewport,
        @Param("limit") int limit,
        @Param("srid") int srid
    );

   Optional<Estate> findById(@Param("id") Long id);
}

