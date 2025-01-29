package com.zipsoon.zipsoonapp.repository.mapper;

import com.zipsoon.zipsoonapp.domain.Property;
import org.apache.ibatis.annotations.Mapper;
import org.postgis.Point;

import java.util.List;

@Mapper
public interface PropertyMapper {
    List<Property> findPropertiesWithinDistance(Point coord, int radius);
}