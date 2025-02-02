package com.zipsoon.batch.job.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipsoon.batch.dto.NaverResponseDto;
import com.zipsoon.common.domain.PropertySnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PropertyItemProcessor implements ItemProcessor<NaverResponseDto, List<PropertySnapshot>> {
    private final ObjectMapper objectMapper;

    @Override
    public List<PropertySnapshot> process(NaverResponseDto item) {
        List<PropertySnapshot> snapshots = new ArrayList<>();

        for (NaverResponseDto.ArticleDto article : item.articleList()) {
            try {
                PropertySnapshot snapshot = PropertySnapshot.builder()
                    .platformType(PropertySnapshot.PlatformType.네이버)
                    .platformId(article.articleNo())
                    .rawData(objectMapper.valueToTree(article))
                    .propName(article.articleName())
                    .propType(PropertySnapshot.PropType.of(article.articleRealEstateTypeName()))
                    .tradeType(PropertySnapshot.TradeType.of(article.tradeTypeName()))
                    .price(parsePrice(article.dealOrWarrantPrc()))
                    .rentPrice(parsePrice(article.rentPrc()))
                    .areaMeter(BigDecimal.valueOf(article.area1()))
                    .areaPyeong(BigDecimal.valueOf(article.area2()))
                    .location(createPoint(article.longitude(), article.latitude()))
                    .address(article.detailAddress())
                    .tags(Arrays.asList(article.tagList()))
                    .dongCode(item.dongCode())
                    .createdAt(LocalDateTime.now())
                    .build();
                snapshots.add(snapshot);
            } catch (Exception e) {
                log.error("Failed to process article {}: {}", article.articleNo(), e.getMessage());
            }
        }

        return snapshots;
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
        return new GeometryFactory().createPoint(
            new Coordinate(Double.parseDouble(longitude), Double.parseDouble(latitude))
        );
    }

}
