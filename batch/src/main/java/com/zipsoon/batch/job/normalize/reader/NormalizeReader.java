package com.zipsoon.batch.job.normalize.reader;

import com.zipsoon.batch.infrastructure.repository.normalize.NormalizeRepository;
import com.zipsoon.batch.domain.score.ScoreType;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NormalizeReader implements ItemReader<ScoreType> {

    private final NormalizeRepository normalizeRepository;
    private Iterator<ScoreType> iterator;

    @Override
    public ScoreType read() {
        if (iterator == null) {
            List<ScoreType> scoreTypes = normalizeRepository.findAllActiveScoreTypes();
            iterator = scoreTypes.iterator();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }
}