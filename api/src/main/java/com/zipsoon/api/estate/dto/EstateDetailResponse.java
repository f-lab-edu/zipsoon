package com.zipsoon.api.estate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.zipsoon.common.domain.EstateSnapshot;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.util.List;

public record EstateDetailResponse(
    Long id,
    String name,
    EstateSnapshot.EstateType type,
    EstateSnapshot.TradeType tradeType,
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
    public static EstateDetailResponse from(EstateSnapshot snapshot) {
        return new EstateDetailResponse(
            snapshot.getId(),
            snapshot.getEstateName(),
            snapshot.getEstateType(),
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
