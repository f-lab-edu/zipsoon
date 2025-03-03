package com.zipsoon.batch.job.source.reader;

import com.zipsoon.batch.application.service.source.collector.SourceCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SourceReader implements ItemReader<SourceCollector> {
    private final List<SourceCollector> collectors;
    private Iterator<SourceCollector> collectorsIterator;

    @Override
    public SourceCollector read() {
        if (collectorsIterator == null) {
            collectorsIterator = collectors.iterator();
        }

        return collectorsIterator.hasNext() ? collectorsIterator.next() : null;
    }

}
