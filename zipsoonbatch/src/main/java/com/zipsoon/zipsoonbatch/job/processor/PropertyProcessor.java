package com.zipsoon.zipsoonbatch.job.processor;

import com.zipsoon.zipsoonbatch.domain.PlatformType;
import com.zipsoon.zipsoonbatch.domain.Property;
import com.zipsoon.zipsoonbatch.domain.PropertyStatusType;
import com.zipsoon.zipsoonbatch.job.reader.NaverResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDateTime;


@Slf4j
@RequiredArgsConstructor
public class PropertyProcessor implements ItemProcessor<NaverResponseDto.ArticleDto, Property> {
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    public Property process(NaverResponseDto.ArticleDto item) {
        LocalDateTime now = LocalDateTime.now();

        return Property.builder()
                .id(null)   // autoincrement
                .platformType(PlatformType.NAVER)
                .platformId(item.getArticleNo())
                .articleName(item.getArticleName())
                .articleStatus(item.getArticleStatus())
                .realEstateTypeCode(item.getRealEstateTypeCode())
                .realEstateTypeName(item.getRealEstateTypeName())
                .articleRealEstateTypeCode(item.getArticleRealEstateTypeCode())
                .articleRealEstateTypeName(item.getArticleRealEstateTypeName())
                .tradeTypeCode(item.getTradeTypeCode())
                .tradeTypeName(item.getTradeTypeName())
                .verificationTypeCode(item.getVerificationTypeCode())
                .floorInfo(item.getFloorInfo())
                .priceChangeState(item.getPriceChangeState())
                .isPriceModification(item.isPriceModification())
                .price(item.getDealOrWarrantPrc())
                .rentPrc(item.getRentPrc())
                .dealOrWarrantPrc(item.getDealOrWarrantPrc())
                .areaName(item.getAreaName())
                .area1(item.getArea1())
                .area2(item.getArea2())
                .direction(item.getDirection())
                .articleConfirmYmd(item.getArticleConfirmYmd())
                .representativeImgUrl(item.getRepresentativeImgUrl())
                .articleFeatureDesc(item.getArticleFeatureDesc())
                .tags(item.getTagList())
                .buildingName(item.getBuildingName())
                .sameAddrCnt(item.getSameAddrCnt())
                .sameAddrDirectCnt(item.getSameAddrDirectCnt())
                .sameAddrMaxPrc(item.getSameAddrMaxPrc())
                .sameAddrMinPrc(item.getSameAddrMinPrc())
                .cpid(item.getCpid())
                .cpName(item.getCpName())
                .cpPcArticleUrl(item.getCpPcArticleUrl())
                .cpPcArticleBridgeUrl(item.getCpPcArticleBridgeUrl())
                .cpPcArticleLinkUseAtArticleTitleYn(item.getCpPcArticleLinkUseAtArticleTitleYn())
                .cpPcArticleLinkUseAtCpNameYn(item.getCpPcArticleLinkUseAtCpNameYn())
                .cpMobileArticleUrl(item.getCpMobileArticleUrl())
                .cpMobileArticleLinkUseAtArticleTitleYn(item.getCpMobileArticleLinkUseAtArticleTitleYn())
                .cpMobileArticleLinkUseAtCpNameYn(item.getCpMobileArticleLinkUseAtCpNameYn())
                .isLocationShow(item.getIsLocationShow())
                .realtorName(item.getRealtorName())
                .realtorId(item.getRealtorId())
                .tradeCheckedByOwner(item.getTradeCheckedByOwner())
                .isDirectTrade(item.getIsDirectTrade())
                .isInterest(item.getIsInterest())
                .isComplex(item.getIsComplex())
                .detailAddress(item.getDetailAddress())
                .detailAddressYn(item.getDetailAddressYn())
                .virtualAddressYn(item.getVirtualAddressYn())
                .isVrExposed(item.getIsVrExposed())
                .location(createPoint(item.getLongitude(), item.getLatitude()))
                .status(PropertyStatusType.ACTIVE)
                .lastChecked(now)
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(null)    // set when deleted
                .build();

    }

    private Point createPoint(String longitude, String latitude) {
        if (longitude == null || latitude == null) {
            throw new IllegalArgumentException("Coordinates must not be null");
        }
        return geometryFactory.createPoint(new Coordinate(
                Double.parseDouble(longitude),
                Double.parseDouble(latitude)
        ));
    }
}
