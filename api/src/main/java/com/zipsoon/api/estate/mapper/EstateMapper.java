package com.zipsoon.api.estate.mapper;

import com.zipsoon.api.estate.dto.ViewportRequest;
import com.zipsoon.common.domain.EstateSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PropertyMapper {
    List<EstateSnapshot> findInViewport(
        @Param("viewport") ViewportRequest viewport,
        @Param("limit") int limit
    );

   @Select("""
       SELECT *
       FROM property_snapshot
       WHERE id = #{id}
   """)
   Optional<EstateSnapshot> findById(@Param("id") Long id);
}

