package com.zipsoon.zipsoonapp.repository;

import com.zipsoon.zipsoonapp.domain.Property;
import com.zipsoon.zipsoonapp.repository.mapper.PropertyMapper;
import lombok.RequiredArgsConstructor;
import org.postgis.Point;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PropertyRepository {
    private final PropertyMapper propertyMapper;

    public List<Property> findPropertiesWithinDistance(Point coord, int radius) {
        return propertyMapper.findPropertiesWithinDistance(coord, radius);
    }
}