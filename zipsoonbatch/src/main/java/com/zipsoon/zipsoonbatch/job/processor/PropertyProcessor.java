package com.zipsoon.zipsoonbatch.job.processor;

import com.zipsoon.zipsoonbatch.domain.PlatformType;
import com.zipsoon.zipsoonbatch.domain.Property;
import com.zipsoon.zipsoonbatch.domain.PropertyStatusType;
import com.zipsoon.zipsoonbatch.job.reader.NaverArticleResponseDto;
import com.zipsoon.zipsoonbatch.repository.PropertyRepository;
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
public class PropertyProcessor implements ItemProcessor<NaverArticleResponseDto.ArticleDto, Property> {
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    private final PropertyRepository propertyRepository;

    @Override
    public Property process(NaverArticleResponseDto.ArticleDto item) {
        log.debug("Processing article: {}", item.getArticleNo());

        Point location = createPoint(item.getLongitude(), item.getLatitude());

        Property property = Property.builder()
            .platformType(PlatformType.NAVER)
            .platformId(item.getArticleNo())
            .name(item.getArticleName())
            .type(item.getRealEstateTypeName())
            .tradeType(item.getTradeTypeName())
            .tradeTypeCode(item.getTradeTypeCode())
            .price(item.getDealOrWarrantPrc())
            .area(item.getArea1())
            .areaP(item.getArea2())
            .location(location)
            .floorInfo(item.getFloorInfo())
            .direction(item.getDirection())
            .buildingName(item.getBuildingName())
            .priceChangeState(item.getPriceChangeState())
            .verificationType(item.getVerificationTypeCode())
            .realtorName(item.getRealtorName())
            .featureDescription(item.getArticleFeatureDesc())
            .tags(item.getTagList())
            .imageUrl(item.getRepresentativeImgUrl())
            .sameAddrCount(item.getSameAddrCnt())
            .sameAddrMaxPrice(item.getSameAddrMaxPrc())
            .sameAddrMinPrice(item.getSameAddrMinPrc())
            .status(PropertyStatusType.ACTIVE)
            .lastChecked(LocalDateTime.now())
            .build();

        // 기존 매물이 있는지 확인
        propertyRepository.findByPlatformTypeAndPlatformId(
                PlatformType.NAVER.name(),
                item.getArticleNo()
            )
            .ifPresent(existingProperty -> {
                property.setId(existingProperty.getId());
                property.setCreatedAt(existingProperty.getCreatedAt());
            });

        return property;
    }

    private Point createPoint(String longitude, String latitude) {
        double lon = Double.parseDouble(longitude);
        double lat = Double.parseDouble(latitude);
        return geometryFactory.createPoint(new Coordinate(lon, lat));
    }
}