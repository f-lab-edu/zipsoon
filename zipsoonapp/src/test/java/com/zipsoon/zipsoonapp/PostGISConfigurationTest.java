package com.zipsoon.zipsoonapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zipsoon.zipsoonapp.config.jackson.PointMixin;
import com.zipsoon.zipsoonapp.domain.Property;
import com.zipsoon.zipsoonapp.repository.mapper.PropertyMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.postgis.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class PostGISConfigurationTest {
    @Autowired
    private PropertyMapper propertyMapper;

   private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .addMixIn(org.postgis.Point.class, PointMixin.class)
            .registerModule(new JavaTimeModule());

    @Test
    void shouldHandleGeometryType() throws JsonProcessingException {
        Point coord = new Point(126.96, 37.572);    // 독립문역 사거리
        int radius = 100;  // 미터

        List<Property> properties = propertyMapper.findPropertiesWithinDistance(coord, radius);

        String prettyJson = objectMapper.writeValueAsString(properties);
        log.info("Properties within {}m of ({}, {}): total {} found\n{}", radius, coord.getX(), coord.getY(), properties.size(), prettyJson);

        assertThat(properties).isNotNull();
    }
}