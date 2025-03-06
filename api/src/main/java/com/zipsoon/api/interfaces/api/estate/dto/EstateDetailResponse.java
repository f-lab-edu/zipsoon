package com.zipsoon.api.interfaces.api.estate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zipsoon.common.domain.Estate;
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
    List<String> imageUrls,
    ScoreDetails score,
    String platformId,
    boolean isFavorite
) {
    public static EstateDetailResponse from(Estate estate, ScoreDetails scoreDetails, boolean isFavorite) {
        return new EstateDetailResponse(
            estate.getId(),
            estate.getEstateName(),
            estate.getEstateType().getKoreanName(),
            estate.getTradeType().getKoreanName(),
            estate.getPrice(),
            estate.getRentPrice(),
            estate.getAreaMeter(),
            estate.getAreaPyeong(),
            ((Point) estate.getLocation()).getY(),
            ((Point) estate.getLocation()).getX(),
            estate.getAddress(),
            estate.getTags(),
            estate.getImageUrls(),
            scoreDetails,
            estate.getPlatformId(),
            isFavorite
        );
    }
}
