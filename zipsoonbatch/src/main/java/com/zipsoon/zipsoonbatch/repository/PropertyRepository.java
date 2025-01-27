package com.zipsoon.zipsoonbatch.repository;

import com.zipsoon.zipsoonbatch.domain.Property;
import com.zipsoon.zipsoonbatch.domain.UpsertResult;
import com.zipsoon.zipsoonbatch.repository.mapper.PropertyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PropertyRepository {
    private final PropertyMapper propertyMapper;

    public Optional<Property> findById(Long id) {
        return propertyMapper.findById(id);
    }

    public void update(Property property) {
        propertyMapper.update(property);
    }

    public UpsertResult upsert(Property property) {
        int affected = propertyMapper.insert(property);
        Property savedProperty = propertyMapper.findByPlatformAndId(
                property.getPlatformType().name(),
                property.getPlatformId()
        ).orElseThrow(() -> new IllegalStateException("Property not found after upsert"));

        return new UpsertResult(
                savedProperty.getId(),
                affected == 0 ? "UPDATE" : "INSERT"
        );
    }
}