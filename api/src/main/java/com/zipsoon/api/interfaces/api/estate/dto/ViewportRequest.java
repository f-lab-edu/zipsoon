package com.zipsoon.api.interfaces.api.estate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "지도 뷰포트 요청 정보")
public record ViewportRequest(
    @Schema(description = "남서쪽 경도", example = "126.7823")
    @NotNull @Min(-180) @Max(180) Double swLng,
    
    @Schema(description = "남서쪽 위도", example = "37.4563")
    @NotNull @Min(-90) @Max(90) Double swLat,
    
    @Schema(description = "북동쪽 경도", example = "127.1837")
    @NotNull @Min(-180) @Max(180) Double neLng,
    
    @Schema(description = "북동쪽 위도", example = "37.6821")
    @NotNull @Min(-90) @Max(90) Double neLat,
    
    @Schema(description = "지도 줌 레벨", example = "14")
    @NotNull @Min(1) @Max(22) Integer zoom
) {}