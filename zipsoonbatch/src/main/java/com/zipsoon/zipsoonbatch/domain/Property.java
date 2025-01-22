package com.zipsoon.zipsoonbatch.domain;

import lombok.Builder;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Getter
@Builder
public class Property {
    private Long id;
    private PlatformType platformType;
    private String platformId;
    private String name;
    private String type;
    private String tradeType;
    private String tradeTypeCode;
    private String price;
    private Double area;
    private Double areaP;
    private Point location;
    private String address;
    private String floorInfo;
    private String direction;
    private String buildingName;
    private String ageType;
    private String priceChangeState;
    private String verificationType;
    private String realtorName;
    private String featureDescription;
    private String[] tags;
    private String imageUrl;
    private Integer sameAddrCount;
    private String sameAddrMaxPrice;
    private String sameAddrMinPrice;
    private PropertyStatusType status;
    private LocalDateTime lastChecked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}