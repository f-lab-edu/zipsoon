package com.zipsoon.common.domain;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.locationtech.jts.geom.Geometry;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PropertySnapshot {

    private Long id;

    private PlatformType platformType;

    private String platformId;

    private JsonNode rawData;

    private String propName;

    private PropType propType;

    private TradeType tradeType;

    private BigDecimal price;

    private BigDecimal rentPrice;

    private BigDecimal areaMeter;

    private BigDecimal areaPyeong;

    private Geometry location;

    private String address;

    private List<String> tags;

    private String dongCode;

    private LocalDateTime createdAt;

    public enum PlatformType {
        네이버("네이버"),
        직방("직방");

        private final String koreanName;

        PlatformType(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() {
            return koreanName;
        }

        public static PlatformType of(String koreanName) {
            for (PlatformType platformType: values()) {
                if (platformType.koreanName.equals(koreanName)) {
                    return platformType;
                }
            }
            return null;
        }
    }

    public enum PropType {
        아파트("아파트"),
        오피스텔("오피스텔"),
        빌라("빌라");

        private final String koreanName;

        PropType(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() {
            return koreanName;
        }

        public static PropType of(String koreanName) {
            for (PropType propType: values()) {
                if (propType.koreanName.equals(koreanName)) {
                    return propType;
                }
            }
            return null;
        }
    }

    public enum TradeType {
        매매("매매"),
        전세("전세"),
        월세("월세");

        private final String koreanName;

        TradeType(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() {
            return koreanName;
        }

        public static TradeType of(String koreanName) {
            for (TradeType tradeType: values()) {
                if (tradeType.koreanName.equals(koreanName)) {
                    return tradeType;
                }
            }
            return null;
        }
    }

}