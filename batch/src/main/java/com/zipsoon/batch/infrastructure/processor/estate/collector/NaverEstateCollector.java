package com.zipsoon.batch.infrastructure.processor.estate.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipsoon.batch.application.service.estate.collector.EstateCollector;
import com.zipsoon.batch.infrastructure.external.naver.NaverLandClient;
import com.zipsoon.batch.infrastructure.external.naver.vo.NaverLandResponseVO;
import com.zipsoon.common.domain.Estate;
import com.zipsoon.common.domain.EstateType;
import com.zipsoon.common.domain.PlatformType;
import com.zipsoon.common.domain.TradeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverEstateCollector implements EstateCollector {

    private final static String NAVER_PREFIX_URL = "https://landthumb-phinf.pstatic.net/";

    private final NaverLandClient naverLandClient;
    private final ObjectMapper objectMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Override
    public String getPlatformName() {
        return PlatformType.네이버.name();
    }

    @Override
    public List<Estate> collect(String dongCode, int page) {
        NaverLandResponseVO response = naverLandClient.get(dongCode, page);

        if (response == null || response.articleList() == null) {
            log.warn("No data received from Naver for dongCode: {}, page: {}", dongCode, page);
            return List.of();
        }

        return Arrays.stream(response.articleList())
                    .map(article -> convertToSnapshot(article, dongCode))
                    .toList();
    }

    @Override
    public boolean hasMoreData(String dongCode, int page) {
        NaverLandResponseVO response = naverLandClient.get(dongCode, page);
        return response != null && response.isMoreData();
    }

    private Estate convertToSnapshot(NaverLandResponseVO.NaverLandResponseArticle article, String dongCode) {
        try {
            return Estate.builder()
                    .platformType(PlatformType.네이버)
                    .platformId(article.articleNo())
                    .rawData(objectMapper.valueToTree(article))
                    .estateName(article.articleName())
                    .estateType(EstateType.valueOf(article.realEstateTypeCode()))
                    .tradeType(TradeType.valueOf(article.tradeTypeCode()))
                    .price(parsePrice(article.dealOrWarrantPrc()))
                    .rentPrice(parsePrice(article.rentPrc()))
                    .areaMeter(BigDecimal.valueOf(article.area1()))
                    .areaPyeong(BigDecimal.valueOf(article.area2()))
                    .location(createPoint(article.longitude(), article.latitude()))
                    .address(article.detailAddress())
                    .tags(Arrays.asList(article.tagList()))
                    .imageUrls(List.of(NAVER_PREFIX_URL + article.representativeImgUrl()))
                    .dongCode(dongCode)
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Failed to convert article to Estate: {}", article, e);
            throw new IllegalStateException("매물 정보 변환 실패: " + e.getMessage());
        }
    }

    private BigDecimal parsePrice(String priceString) {
        if (priceString == null || priceString.isEmpty()) {
            return null;
        }

        if (!priceString.contains("억")) {
            return new BigDecimal(priceString.replaceAll("[^0-9]", ""));
        }

        String[] parts = priceString.split("억");
        long billionPart = Long.parseLong(parts[0].trim().replaceAll("[^0-9]", "")) * 10000; // 1억 = 10000만원

        if (parts.length > 1 && !parts[1].trim().isEmpty()) {
            long millionPart = Long.parseLong(parts[1].trim().replaceAll("[^0-9]", ""));
            return new BigDecimal(billionPart + millionPart);
        }

        return new BigDecimal(billionPart);
    }

    private Point createPoint(String longitude, String latitude) {
        if (longitude == null || latitude == null) {
            return null;
        }
        return geometryFactory.createPoint(
            new Coordinate(
                Double.parseDouble(longitude),
                Double.parseDouble(latitude)
            )
        );
    }

}
