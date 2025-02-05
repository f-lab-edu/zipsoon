package com.zipsoon.api.property.mapper;

import com.zipsoon.api.property.dto.ViewportRequest;
import com.zipsoon.common.domain.PropertySnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PropertyMapper {
    List<PropertySnapshot> findInViewport(
        @Param("viewport") ViewportRequest viewport,
        @Param("limit") int limit
    );

   @Select("""
       SELECT *
       FROM property_snapshot
       WHERE id = #{id}
   """)
   Optional<PropertySnapshot> findById(@Param("id") Long id);
}

