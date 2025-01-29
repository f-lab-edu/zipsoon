package com.zipsoon.zipsoonapp.domain;

import lombok.*;
import org.postgis.Point;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Property {
    private Long id;
    private PlatformType platformType;
    private String platformId;
    private String articleName;
    private String articleStatus;
    private String realEstateTypeCode;
    private String realEstateTypeName;
    private String articleRealEstateTypeCode;
    private String articleRealEstateTypeName;
    private String tradeTypeCode;
    private String tradeTypeName;
    private String verificationTypeCode;
    private String floorInfo;
    private String priceChangeState;
    private boolean isPriceModification;
    private String price;
    private String rentPrc;
    private String dealOrWarrantPrc;
    private String areaName;
    private Double area1;
    private Double area2;
    private String direction;
    private String articleConfirmYmd;
    private String representativeImgUrl;
    private String articleFeatureDesc;
    private String[] tags;
    private String buildingName;
    private Integer sameAddrCnt;
    private Integer sameAddrDirectCnt;
    private String sameAddrMaxPrc;
    private String sameAddrMinPrc;
    private String cpid;
    private String cpName;
    private String cpPcArticleUrl;
    private String cpPcArticleBridgeUrl;
    private Boolean cpPcArticleLinkUseAtArticleTitleYn;
    private Boolean cpPcArticleLinkUseAtCpNameYn;
    private String cpMobileArticleUrl;
    private Boolean cpMobileArticleLinkUseAtArticleTitleYn;
    private Boolean cpMobileArticleLinkUseAtCpNameYn;
    private Boolean isLocationShow;
    private String realtorName;
    private String realtorId;
    private Boolean tradeCheckedByOwner;
    private Boolean isDirectTrade;
    private Boolean isInterest;
    private Boolean isComplex;
    private String detailAddress;
    private String detailAddressYn;
    private String virtualAddressYn;
    private Boolean isVrExposed;
    private Point location;
    private PropertyStatusType status;
    private LocalDateTime lastChecked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}