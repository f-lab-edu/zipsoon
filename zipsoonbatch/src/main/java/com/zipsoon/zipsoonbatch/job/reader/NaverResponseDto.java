package com.zipsoon.zipsoonbatch.job.reader;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverArticleResponseDto {
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
        private boolean isPriceModification;
        private String dealOrWarrantPrc;
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
        private String sameAddrMaxPrc;
        private String sameAddrMinPrc;
        private String latitude;
        private String longitude;
        private String realtorName;
    }
}