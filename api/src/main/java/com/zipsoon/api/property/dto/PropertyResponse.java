package com.zipsoon.api.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zipsoon.common.domain.PropertySnapshot;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;

@Slf4j
public record PropertyResponse(
    Long id,
    String name,
    PropertySnapshot.PropType type,
    PropertySnapshot.TradeType tradeType,
    BigDecimal price,
    BigDecimal rentPrice,
    BigDecimal area,
    @JsonProperty("lat") double latitude,
    @JsonProperty("lng") double longitude
) {
    public static PropertyResponse from(PropertySnapshot snapshot) {
        log.info("Geometry: {}", snapshot.getLocation());
        Point location = (Point) snapshot.getLocation();
        double latitude = location != null ? location.getY() : 0.0;
        double longitude = location != null ? location.getX() : 0.0;
        log.info("location: {}, latitude: {}, longitude: {}", location, latitude, longitude);
        return new PropertyResponse(
            snapshot.getId(),
            snapshot.getPropName(),
            snapshot.getPropType(),
            snapshot.getTradeType(),
            snapshot.getPrice(),
            snapshot.getRentPrice(),
            snapshot.getAreaMeter(),
            ((Point) snapshot.getLocation()).getY(),
            ((Point) snapshot.getLocation()).getX()
        );
    }
}
