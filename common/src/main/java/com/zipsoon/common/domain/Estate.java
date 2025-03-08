package com.zipsoon.common.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.zipsoon.common.domain.value.Area;
import com.zipsoon.common.domain.value.Price;
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

    private Price price;

    private Price rentPrice;

    private Area area;

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
     * @param area1 면적(제곱미터)
     * @param area2 면적(평)
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
            BigDecimal area1, // 평방미터
            BigDecimal area2, // 평
            Geometry location,
            String address,
            String dongCode,
            List<String> imageUrls) {

        // 필수 속성 유효성 검증
        if (platformType == null || platformId == null || location == null) {
            throw new IllegalArgumentException("필수 매물 속성(platformType, platformId, location)은 null일 수 없습니다");
        }
        
        // 거래 유형에 따른 가격 유효성 검증
        if ((tradeType == TradeType.A1 || tradeType == TradeType.B1) && price == null) {
            throw new IllegalArgumentException("매매, 전세 유형은 가격(price)이 필수입니다");
        }
        if ((tradeType == TradeType.B2 || tradeType == TradeType.B3) && rentPrice == null) {
            throw new IllegalArgumentException("월세, 단기임대 유형은 임대료(rentPrice)가 필수입니다");
        }
        
        // 면적 정보 처리 - area1(제곱미터)가 0이거나 null이면 area2(평)에서 변환
        Area area = null;
        if (area1 != null && area1.compareTo(BigDecimal.ZERO) > 0) {
            area = Area.ofSquareMeters(area1);
        } else if (area2 != null && area2.compareTo(BigDecimal.ZERO) > 0) {
            area = Area.ofPyeong(area2);
        }

        return Estate.builder()
                .platformType(platformType)
                .platformId(platformId)
                .rawData(rawData)
                .estateName(estateName)
                .estateType(estateType)
                .tradeType(tradeType)
                .price(price != null ? Price.of(price) : null)
                .rentPrice(rentPrice != null ? Price.of(rentPrice) : null)
                .area(area)
                .location(location)
                .address(address)
                .dongCode(dongCode)
                .imageUrls(imageUrls)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 가격 원시값 반환 (호환성)
     */
    public BigDecimal getPrice() {
        return price != null ? price.amount() : null;
    }
    
    /**
     * 임대료 원시값 반환 (호환성)
     */
    public BigDecimal getRentPrice() {
        return rentPrice != null ? rentPrice.amount() : null;
    }
    
    /**
     * 면적(제곱미터) 원시값 반환 (호환성)
     */
    public BigDecimal getAreaMeter() {
        return area != null ? area.squareMeters() : null;
    }
    
    /**
     * 면적(평) 원시값 반환 (호환성)
     */
    public BigDecimal getAreaPyeong() {
        return area != null ? area.toPyeong() : null;
    }
}