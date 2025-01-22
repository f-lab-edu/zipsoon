package com.zipsoon.zipsoonbatch.repository.mapper;

import com.zipsoon.zipsoonbatch.domain.Property;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface PropertyMapper {
    Optional<Property> findByPlatformTypeAndPlatformId(
        @Param("platformType") String platformType,
        @Param("platformId") String platformId
    );

    void save(Property property);
}