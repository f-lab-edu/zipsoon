package com.zipsoon.common.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.util.List;

public class EstateUtils {
    private static final String NAVER_PREFIX_URL = "https://landthumb-phinf.pstatic.net/";

    /**
     * 한국식 가격 표기("억" 단위)를 BigDecimal로 변환
     */
    public static BigDecimal parsePrice(String priceString) {
        if (priceString == null || priceString.isEmpty()) {
            return null;
        }

        if (!priceString.contains("억")) {
            return new BigDecimal(priceString.replaceAll("[^0-9]", ""));
        }

        String[] parts = priceString.split("억");
        long billionPart = Long.parseLong(parts[0].trim().replaceAll("[^0-9]", "")) * 10000;

        if (parts.length > 1 && !parts[1].trim().isEmpty()) {
            long millionPart = Long.parseLong(parts[1].trim().replaceAll("[^0-9]", ""));
            return new BigDecimal(billionPart + millionPart);
        }

        return new BigDecimal(billionPart);
    }

    /**
     * 위경도 문자열을 Point 객체로 변환
     */
    public static Point createPoint(String longitude, String latitude, GeometryFactory factory) {
        if (longitude == null || latitude == null) {
            return null;
        }
        return factory.createPoint(
            new Coordinate(
                Double.parseDouble(longitude),
                Double.parseDouble(latitude)
            )
        );
    }

    /**
     * 이미지 URL 처리
     */
    public static List<String> formatImageUrls(String imageUrl) {
        if (imageUrl == null || "null".equals(imageUrl)) {
            return null;
        }
        return List.of(NAVER_PREFIX_URL + imageUrl);
    }

    /**
     * Double 값을 BigDecimal로 안전하게 변환
     */
    public static BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}