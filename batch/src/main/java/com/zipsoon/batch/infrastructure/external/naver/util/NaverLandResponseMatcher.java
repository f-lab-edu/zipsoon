package com.zipsoon.batch.infrastructure.external.naver.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipsoon.batch.infrastructure.external.naver.vo.NaverLandResponseVO;
import com.zipsoon.common.domain.Estate;
import com.zipsoon.common.domain.EstateType;
import com.zipsoon.common.domain.PlatformType;
import com.zipsoon.common.domain.TradeType;
import com.zipsoon.common.util.EstateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverLandResponseMatcher {
    private final ObjectMapper objectMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * 네이버 부동산 API 응답을 Estate 객체로 변환
     */
    public Estate toEstate(NaverLandResponseVO.NaverLandResponseArticle article, String dongCode) {
        try {
            return Estate.of(
                PlatformType.네이버,
                article.articleNo(),
                objectMapper.valueToTree(article),
                article.articleName(),
                EstateType.valueOf(article.realEstateTypeCode()),
                TradeType.valueOf(article.tradeTypeCode()),
                EstateUtils.parsePrice(article.dealOrWarrantPrc()),
                EstateUtils.parsePrice(article.rentPrc()),
                EstateUtils.toBigDecimal(article.area1()),
                EstateUtils.toBigDecimal(article.area2()),
                EstateUtils.createPoint(article.longitude(), article.latitude(), geometryFactory),
                article.detailAddress(),
                dongCode,
                EstateUtils.formatImageUrls(article.representativeImgUrl())
            );
        } catch (Exception e) {
            log.error("Failed to convert article to Estate: {}", article, e);
            throw new IllegalStateException("매물 정보 변환 실패: " + e.getMessage());
        }
    }
}
