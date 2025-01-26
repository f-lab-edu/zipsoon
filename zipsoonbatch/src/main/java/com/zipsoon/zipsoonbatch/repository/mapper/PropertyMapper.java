package com.zipsoon.zipsoonbatch.repository.mapper;

import com.zipsoon.zipsoonbatch.domain.Property;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface PropertyMapper {
    Optional<Property> findByPlatformAndId(
        @Param("platformType") String platformType,
        @Param("platformId") String platformId
    );

    void updateLastCheckedById(
        @Param("id") Long id,
        @Param("lastChecked") LocalDateTime lastChecked
    );

    void save(Property property);
}