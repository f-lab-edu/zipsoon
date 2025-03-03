package com.zipsoon.batch.source.job.reader;

import com.zipsoon.batch.infrastructure.whichname.source.ScoreSourceCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreSourceReader implements ItemReader<ScoreSourceCollector> {
    private final List<ScoreSourceCollector> collectors;
    private Iterator<ScoreSourceCollector> collectorsIterator;

    @Override
    public ScoreSourceCollector read() {
        if (collectorsIterator == null) {
            collectorsIterator = collectors.iterator();
        }

        return collectorsIterator.hasNext() ? collectorsIterator.next() : null;
    }

}
