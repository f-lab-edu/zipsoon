package com.zipsoon.zipsoonbatch.repository.mapper;

import com.zipsoon.zipsoonbatch.domain.Property;
import com.zipsoon.zipsoonbatch.domain.UpsertResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface PropertyMapper {
    Optional<Property> findById(@Param("id") Long id);
    Optional<Property> findByPlatformAndId(
        @Param("platformType") String platformType,
        @Param("platformId") String platformId
    );
    int insert(Property property);
    void update(Property property);
}