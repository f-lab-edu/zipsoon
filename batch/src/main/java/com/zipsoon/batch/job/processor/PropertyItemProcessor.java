package com.zipsoon.batch.job.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipsoon.batch.dto.NaverResponseDto;
import com.zipsoon.batch.exception.PropertyProcessingException;
import com.zipsoon.common.domain.PropertySnapshot;
import com.zipsoon.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PropertyItemProcessor implements ItemProcessor<NaverResponseDto, List<PropertySnapshot>> {
    private final ObjectMapper objectMapper;

    @Override
    public List<PropertySnapshot> process(NaverResponseDto item) {
        if (item == null || item.articleList() == null) {
            throw new PropertyProcessingException(
                ErrorCode.EXTERNAL_API_ERROR,
                "Received null or invalid data from Naver API");
        }
        String processingDongCode = item.dongCode();

        try {
            return Arrays.stream(item.articleList())
                .map(article -> convertToSnapshot(article, processingDongCode))
                .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new PropertyProcessingException(
                ErrorCode.EXTERNAL_API_ERROR,
                "Invalid property data encountered: " + e.getMessage(),
                Map.of("NaverResponseDto", item)
            );
        } catch (Exception e) {
            throw new PropertyProcessingException(
                ErrorCode.EXTERNAL_API_ERROR,
                "Unexpected error during property processing: " + e.getMessage(),
                Map.of("item", item, "error", e)
            );
        }
    }

    private PropertySnapshot convertToSnapshot(NaverResponseDto.ArticleDto article, String processingDongCode) {
        try {
            return PropertySnapshot.builder()
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
                    .dongCode(processingDongCode)
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            throw new PropertyProcessingException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    "Failed to convert article to PropertySnapshot: " + e.getMessage(),
                    Map.of("article", article, "error", e)
            );
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
        return new GeometryFactory().createPoint(
            new Coordinate(Double.parseDouble(longitude), Double.parseDouble(latitude))
        );
    }

}
