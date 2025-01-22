package com.zipsoon.zipsoonbatch.repository;

import com.zipsoon.zipsoonbatch.domain.Property;
import com.zipsoon.zipsoonbatch.repository.mapper.PropertyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PropertyRepository {
    private final PropertyMapper propertyMapper;

    public Optional<Property> findByPlatformTypeAndPlatformId(String platformType, String platformId) {
        return propertyMapper.findByPlatformTypeAndPlatformId(platformType, platformId);
    }

    public void save(Property property) {
        propertyMapper.save(property);
    }
}