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
    String type,
    String tradeType,
    BigDecimal price,
    BigDecimal rentPrice,
    BigDecimal areaMeter,
    BigDecimal areaPyeong,
    @JsonProperty("lat") double latitude,
    @JsonProperty("lng") double longitude,
    String address,
    List<String> tags,
    List<String> images,
    ScoreDetails score,
    JsonNode rawData
) {
    public static EstateDetailResponse from(EstateSnapshot snapshot, ScoreDetails scoreDetails) {
        return new EstateDetailResponse(
            snapshot.getId(),
            snapshot.getEstateName(),
            snapshot.getEstateType().getKoreanName(),
            snapshot.getTradeType().getKoreanName(),
            snapshot.getPrice(),
            snapshot.getRentPrice(),
            snapshot.getAreaMeter(),
            snapshot.getAreaPyeong(),
            ((Point) snapshot.getLocation()).getY(),
            ((Point) snapshot.getLocation()).getX(),
            snapshot.getAddress(),
            snapshot.getTags(),
            snapshot.getImageUrls(),
            scoreDetails,
            snapshot.getRawData()
        );
    }
}
