package com.zipsoon.batch.score.domain;

import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDate;

@Getter
@Builder(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Park {
    private String id;
    private String name;
    private String type;
    private Point location;
    private double area;
    private LocalDate designatedDate;
    private String managementAgency;
}