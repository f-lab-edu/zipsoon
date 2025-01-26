package com.zipsoon.zipsoonbatch.repository;

import com.zipsoon.zipsoonbatch.domain.Property;
import com.zipsoon.zipsoonbatch.repository.mapper.PropertyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class PropertyRepository {
    private final PropertyMapper propertyMapper;

    public Optional<Property> findByPlatformAndId(String platformType, String id) {
        return propertyMapper.findByPlatformAndId(platformType, id);
    }

    public void updateLastCheckedById(Long id, LocalDateTime lastChecked) {
        propertyMapper.updateLastCheckedById(id, lastChecked);
    }

    public void save(Property property) {
        propertyMapper.save(property);
    }
}