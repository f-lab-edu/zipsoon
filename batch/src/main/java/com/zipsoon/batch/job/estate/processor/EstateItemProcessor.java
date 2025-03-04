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
        List<Estate> snapshots = new ArrayList<>();
        int page = 1;

        while (estateCollector.hasMoreData(dongCode, page)) {
            snapshots.addAll(estateCollector.collect(dongCode, page));
            page++;
        }

        return snapshots;
    }
}
