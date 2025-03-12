package com.zipsoon.api.interfaces.api.estate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zipsoon.common.domain.Estate;
import com.zipsoon.common.domain.EstateType;
import com.zipsoon.common.domain.TradeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;

@Schema(description = "매물 기본 정보 응답")
@Slf4j
public record EstateResponse(
    @Schema(description = "매물 ID", example = "1234")
    Long id,
    
    @Schema(description = "매물명", example = "서울 강남구 테헤란로 OO빌딩 203호")
    String name,
    
    @Schema(description = "매물 유형", example = "APT(아파트)")
    EstateType type,
    
    @Schema(description = "거래 유형", example = "B1(전세)")
    TradeType tradeType,
    
    @Schema(description = "매매가/보증금", example = "300000000")
    BigDecimal price,
    
    @Schema(description = "월세", example = "1000000")
    BigDecimal rentPrice,
    
    @Schema(description = "면적(m²)", example = "84.00")
    BigDecimal area,
    
    @Schema(description = "위도", example = "37.0000")
    @JsonProperty("lat") double latitude,
    
    @Schema(description = "경도", example = "126.0000")
    @JsonProperty("lng") double longitude,
    
    @Schema(description = "종합 점수 정보")
    ScoreSummaryResponse score
) {
    public static EstateResponse from(Estate estate, ScoreSummaryResponse scoreSummary) {
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
