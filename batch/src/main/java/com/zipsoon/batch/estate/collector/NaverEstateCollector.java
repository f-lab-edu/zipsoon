package com.zipsoon.batch.estate.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipsoon.batch.infra.naver.NaverLandClient;
import com.zipsoon.batch.infra.naver.vo.NaverLandResponse;
import com.zipsoon.common.domain.EstateSnapshot;
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
        return EstateSnapshot.PlatformType.네이버.name();
    }

    @Override
    public List<EstateSnapshot> collect(String dongCode, int page) {
        NaverLandResponse response = naverLandClient.get(dongCode, page);

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
        NaverLandResponse response = naverLandClient.get(dongCode, page);
        return response != null && response.isMoreData();
    }

    private EstateSnapshot convertToSnapshot(NaverLandResponse.NaverLandResponseArticle article, String dongCode) {
        try {
            return EstateSnapshot.builder()
                    .platformType(EstateSnapshot.PlatformType.네이버)
                    .platformId(article.articleNo())
                    .rawData(objectMapper.valueToTree(article))
                    .estateName(article.articleName())
                    .estateType(EstateSnapshot.EstateType.valueOf(article.realEstateTypeCode()))
                    .tradeType(EstateSnapshot.TradeType.valueOf(article.tradeTypeCode()))
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
            log.error("Failed to convert article to EstateSnapshot: {}", article, e);
            throw new IllegalStateException("매물 정보 변환 실패: " + e.getMessage());
        }
    }

    private BigDecimal parsePrice(String priceString) {
        if (priceString == null || priceString.isEmpty()) {
            return null;
        }
        return new BigDecimal(priceString.replaceAll("[^0-9]", ""));
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
