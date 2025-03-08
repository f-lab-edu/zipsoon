package com.zipsoon.batch.job.estate.processor;

import com.zipsoon.batch.infrastructure.processor.estate.collector.NaverEstateCollector;
import com.zipsoon.common.domain.Estate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstateItemProcessor implements ItemProcessor<String, List<Estate>> {
    private final NaverEstateCollector estateCollector;

    @Override
    public List<Estate> process(String dongCode) {
        log.debug("[BATCH:STEP-PROCESSOR] 법정동코드 {} 매물 수집 시작", dongCode);
        
        List<Estate> snapshots = new ArrayList<>();
        int page = 1;

        while (estateCollector.hasMoreData(dongCode, page)) {
            List<Estate> pageData = estateCollector.collect(dongCode, page);
            snapshots.addAll(pageData);
            log.debug("[BATCH:STEP-PROCESSOR] 법정동코드 {} - 페이지 {} 매물 {}개 수집", dongCode, page, pageData.size());
            page++;
        }
        
        log.info("[BATCH:STEP-PROCESSOR] 법정동코드 {} 매물 수집 완료 - 총 {}개 수집됨", dongCode, snapshots.size());
        return snapshots;
    }
}
