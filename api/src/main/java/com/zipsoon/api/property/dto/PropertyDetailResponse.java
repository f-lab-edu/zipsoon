package com.zipsoon.api.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.zipsoon.common.domain.PropertySnapshot;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.util.List;

public record PropertyDetailResponse(
    Long id,
    String name,
    PropertySnapshot.PropType type,
    PropertySnapshot.TradeType tradeType,
    BigDecimal price,
    BigDecimal rentPrice,
    BigDecimal areaMeter,
    BigDecimal areaPyeong,
    @JsonProperty("lat") double latitude,
    @JsonProperty("lng") double longitude,
    String address,
    List<String> tags,
    JsonNode rawData
) {
    public static PropertyDetailResponse from(PropertySnapshot snapshot) {
        return new PropertyDetailResponse(
            snapshot.getId(),
            snapshot.getPropName(),
            snapshot.getPropType(),
            snapshot.getTradeType(),
            snapshot.getPrice(),
            snapshot.getRentPrice(),
            snapshot.getAreaMeter(),
            snapshot.getAreaPyeong(),
            ((Point) snapshot.getLocation()).getY(),
            ((Point) snapshot.getLocation()).getX(),
            snapshot.getAddress(),
            snapshot.getTags(),
            snapshot.getRawData()
        );
    }
}
