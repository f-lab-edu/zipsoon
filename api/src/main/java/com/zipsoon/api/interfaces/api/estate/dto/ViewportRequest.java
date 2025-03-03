package com.zipsoon.api.interfaces.api.estate.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ViewportRequest(
    @NotNull @Min(-180) @Max(180) Double swLng,
    @NotNull @Min(-90) @Max(90) Double swLat,
    @NotNull @Min(-180) @Max(180) Double neLng,
    @NotNull @Min(-90) @Max(90) Double neLat,
    @NotNull @Min(1) @Max(22) Integer zoom
) {}