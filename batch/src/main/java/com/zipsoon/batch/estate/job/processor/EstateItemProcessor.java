package com.zipsoon.batch.estate.job.processor;

import com.zipsoon.batch.estate.collector.NaverEstateCollector;
import com.zipsoon.common.domain.EstateSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstateItemProcessor implements ItemProcessor<String, List<EstateSnapshot>> {
    private final NaverEstateCollector estateCollector;
    private static final int MAX_PAGE = 50;

    @Override
    public List<EstateSnapshot> process(String dongCode) {
        List<EstateSnapshot> snapshots = new ArrayList<>();
        int page = 1;

        while (page <= MAX_PAGE && estateCollector.hasMoreData(dongCode, page)) {
            snapshots.addAll(estateCollector.collect(dongCode, page));
            page++;
        }

        return snapshots;
    }
}
