package com.zipsoon.batch.estate.job.reader;

import com.zipsoon.batch.domain.estate.DongCode;
import com.zipsoon.batch.infrastructure.external.naver.vo.NaverLandResponseVO;
import com.zipsoon.batch.estate.service.DongCodeService;
import com.zipsoon.batch.infrastructure.external.naver.NaverLandClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstateItemReader implements ItemReader<NaverLandResponseVO> {
    private static final int MAX_PAGE_LIMIT = 50;

    private final NaverLandClient naverLandClient;
    private final DongCodeService dongCodeService;

    private List<DongCode> dongCodes;
    private int currentDongIndex = 0;
    private int currentPage = 1;
    private boolean hasMore = true;

    @PostConstruct
    public void init() {
        this.dongCodes = dongCodeService.getAllDongCodes();
        log.info("Initialized with {} dong codes", dongCodes.size());
    }

    @Override
    public NaverLandResponseVO read() {
        if (currentDongIndex >= dongCodes.size()) {
            return null;
        }

        DongCode currentDong = dongCodes.get(currentDongIndex);
        NaverLandResponseVO response = fetchNextPage(currentDong.code());

        if (!hasMore) {
            currentDongIndex++;
            currentPage = 1;
            hasMore = true;
        }

        return response;
    }

    private NaverLandResponseVO fetchNextPage(String dongCode) {
        log.debug("Fetching page {} for dongCode {}", currentPage, dongCode);

        NaverLandResponseVO response = naverLandClient.get(dongCode, currentPage);
        hasMore = response.isMoreData() && currentPage < MAX_PAGE_LIMIT;
        currentPage++;

        return response;
    }
}
