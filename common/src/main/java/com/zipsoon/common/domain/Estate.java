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
public class Estate {

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
    
    /**
     * 외부 플랫폼에서 수집한 매물 정보로 새 객체를 생성합니다.
     *
     * @param platformType 플랫폼 유형 (네이버, 직방 등)
     * @param platformId 플랫폼 고유 ID
     * @param rawData 수집한 원본 데이터
     * @param estateName 매물명
     * @param estateType 매물 유형
     * @param tradeType 거래 유형
     * @param price 가격
     * @param rentPrice 임대료
     * @param areaMeter 면적(제곱미터)
     * @param location 위치 좌표
     * @param address 주소
     * @param dongCode 법정동 코드
     * @param imageUrls 이미지 URL 목록
     * @return 생성된 매물 객체
     */
    public static Estate of(
            PlatformType platformType,
            String platformId,
            JsonNode rawData,
            String estateName,
            EstateType estateType,
            TradeType tradeType,
            BigDecimal price,
            BigDecimal rentPrice,
            BigDecimal areaMeter,
            Geometry location,
            String address,
            String dongCode,
            List<String> imageUrls) {
        
        // 필수 속성 유효성 검증
        if (platformType == null || platformId == null || location == null) {
            throw new IllegalArgumentException("필수 매물 속성(platformType, platformId, location)은 null일 수 없습니다");
        }
        
        // 거래 유형에 따른 가격 유효성 검증
        if (tradeType == TradeType.A1 && price == null) {
            throw new IllegalArgumentException("매매 유형은 가격(price)이 필수입니다");
        }
        if ((tradeType == TradeType.B1 || tradeType == TradeType.B2) && rentPrice == null) {
            throw new IllegalArgumentException("임대 유형은 임대료(rentPrice)가 필수입니다");
        }
        
        return Estate.builder()
                .platformType(platformType)
                .platformId(platformId)
                .rawData(rawData)
                .estateName(estateName)
                .estateType(estateType)
                .tradeType(tradeType)
                .price(price)
                .rentPrice(rentPrice)
                .areaMeter(areaMeter)
                .areaPyeong(areaMeter != null ? areaMeter.multiply(BigDecimal.valueOf(0.3025)) : null)
                .location(location)
                .address(address)
                .dongCode(dongCode)
                .imageUrls(imageUrls)
                .createdAt(LocalDateTime.now())
                .build();
    }
}