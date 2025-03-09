package com.zipsoon.batch.infrastructure.processor.estate.collector;

import com.zipsoon.batch.application.service.estate.collector.EstateCollector;
import com.zipsoon.batch.infrastructure.external.naver.NaverLandClient;
import com.zipsoon.batch.infrastructure.external.naver.util.NaverLandResponseMatcher;
import com.zipsoon.batch.infrastructure.external.naver.vo.NaverLandResponseVO;
import com.zipsoon.common.domain.Estate;
import com.zipsoon.common.domain.PlatformType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverEstateCollector implements EstateCollector {
    private final NaverLandClient naverLandClient;
    private final NaverLandResponseMatcher naverLandResponseMatcher;

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
                    .map(article -> naverLandResponseMatcher.toEstate(article, dongCode))
                    .toList();
    }

    @Override
    public boolean hasMoreData(String dongCode, int page) {
        NaverLandResponseVO response = naverLandClient.get(dongCode, page);
        return response != null && response.isMoreData();
    }

}
