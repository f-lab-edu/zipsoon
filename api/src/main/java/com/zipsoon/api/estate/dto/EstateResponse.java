package com.zipsoon.api.estate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zipsoon.common.domain.Estate;
import com.zipsoon.common.domain.EstateType;
import com.zipsoon.common.domain.TradeType;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;

@Slf4j
public record EstateResponse(
    Long id,
    String name,
    EstateType type,
    TradeType tradeType,
    BigDecimal price,
    BigDecimal rentPrice,
    BigDecimal area,
    @JsonProperty("lat") double latitude,
    @JsonProperty("lng") double longitude,
    ScoreSummary score
) {
    public static EstateResponse from(Estate estate, ScoreSummary scoreSummary) {
        return new EstateResponse(
            estate.getId(),
            estate.getEstateName(),
            estate.getEstateType(),
            estate.getTradeType(),
            estate.getPrice(),
            estate.getRentPrice(),
            estate.getAreaMeter(),
            ((Point) estate.getLocation()).getY(),
            ((Point) estate.getLocation()).getX(),
            scoreSummary
        );
    }
}
