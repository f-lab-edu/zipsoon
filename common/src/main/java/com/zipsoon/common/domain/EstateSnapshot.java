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
public class EstateSnapshot {

    private Long id;

    private PlatformType platformType;

    private String platformId;

    private JsonNode rawData;

    private String estateName;

    private EstateType estateType;

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

    private List<String> imageUrls;

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

    public enum EstateType {
        APT("아파트"),
        OPST("오피스텔"),
        VL("빌라"),
        ABYG("아파트분양권"),
        OBYG("오피스텔분양권"),
        JGC("재건축"),
        JWJT("전원주택"),
        DDDGG("단독/다가구"),
        SGJT("상가주택"),
        HOJT("한옥주택"),
        JGB("재개발"),
        OR("원룸"),
        SG("상가"),
        SMS("사무실"),
        GJCG("공장/창고"),
        GM("건물"),
        TJ("토지"),
        APTHGJ("지식산업센터");

        private final String koreanName;

        EstateType(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() {
            return koreanName;
        }

        public static EstateType of(String koreanName) {
            for (EstateType estateType : values()) {
                if (estateType.koreanName.equals(koreanName)) {
                    return estateType;
                }
            }
            return null;
        }
    }

    public enum TradeType {
        A1("매매"),
        B1("전세"),
        B2("월세"),
        B3("단기임대");

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