package com.zipsoon.zipsoonbatch.job.reader;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverResponseDto {
    @JsonProperty("isMoreData")
    private boolean isMoreData;
    private ArticleDto[] articleList;

    @Getter
    @NoArgsConstructor
    public static class ArticleDto {
        private String articleNo;
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
        @JsonProperty("isPriceModification")
        private boolean isPriceModification;
        private String dealOrWarrantPrc;
        private String rentPrc;
        private String areaName;
        private Double area1;
        private Double area2;
        private String direction;
        private String articleConfirmYmd;
        private String representativeImgUrl;
        private String articleFeatureDesc;
        private String[] tagList;
        private String buildingName;
        private Integer sameAddrCnt;
        private Integer sameAddrDirectCnt;
        private String sameAddrMaxPrc;
        private String sameAddrMinPrc;
        private String cpid;
        private String cpName;
        private String cpPcArticleUrl;
        private String cpPcArticleBridgeUrl;
        @JsonProperty("cpPcArticleLinkUseAtArticleTitleYn")
        private Boolean cpPcArticleLinkUseAtArticleTitleYn;
        @JsonProperty("cpPcArticleLinkUseAtCpNameYn")
        private Boolean cpPcArticleLinkUseAtCpNameYn;
        private String cpMobileArticleUrl;
        @JsonProperty("cpMobileArticleLinkUseAtArticleTitleYn")
        private Boolean cpMobileArticleLinkUseAtArticleTitleYn;
        @JsonProperty("cpMobileArticleLinkUseAtCpNameYn")
        private Boolean cpMobileArticleLinkUseAtCpNameYn;
        private String latitude;
        private String longitude;
        private String realtorName;
        private String realtorId;
        @JsonProperty("tradeCheckedByOwner")
        private Boolean tradeCheckedByOwner;
        @JsonProperty("isDirectTrade")
        private Boolean isDirectTrade;
        @JsonProperty("isInterest")
        private Boolean isInterest;
        @JsonProperty("isLocationShow")
        private Boolean isLocationShow;
        @JsonProperty("isComplex")
        private Boolean isComplex;
        private String detailAddress;
        private String detailAddressYn;
        private String virtualAddressYn;
        @JsonProperty("isVrExposed")
        private Boolean isVrExposed;
    }
}