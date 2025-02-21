package com.zipsoon.batch.job.reader;

import com.zipsoon.batch.domain.DongCode;
import com.zipsoon.batch.dto.NaverResponseDto;
import com.zipsoon.batch.service.DongCodeService;
import com.zipsoon.batch.service.NaverClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstateItemReader implements ItemReader<NaverResponseDto> {
    private static final int MAX_PAGE_LIMIT = 50;

    private final NaverClient naverClient;
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
    public NaverResponseDto read() {
        if (currentDongIndex >= dongCodes.size()) {
            return null;
        }

        DongCode currentDong = dongCodes.get(currentDongIndex);
        NaverResponseDto response = fetchNextPage(currentDong.code());

        if (!hasMore) {
            currentDongIndex++;
            currentPage = 1;
            hasMore = true;
        }

        return response;
    }

    private NaverResponseDto fetchNextPage(String dongCode) {
        log.debug("Fetching page {} for dongCode {}", currentPage, dongCode);

        NaverResponseDto response = naverClient.get(dongCode, currentPage);
        hasMore = response.isMoreData() && currentPage < MAX_PAGE_LIMIT;
        currentPage++;

        return response;
    }
}
