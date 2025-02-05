package com.zipsoon.api.property.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

public record ViewportRequest(
    @NotNull @Min(-180) @Max(180) Double swLng,
    @NotNull @Min(-90) @Max(90) Double swLat,
    @NotNull @Min(-180) @Max(180) Double neLng,
    @NotNull @Min(-90) @Max(90) Double neLat,
    @NotNull @Min(1) @Max(22) Integer zoom
) {
    public Envelope toBoundingBox() {
        return new Envelope(
            new Coordinate(swLng, swLat),
            new Coordinate(neLng, neLat)
        );
    }
}
