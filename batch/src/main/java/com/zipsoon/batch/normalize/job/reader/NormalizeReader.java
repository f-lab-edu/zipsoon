package com.zipsoon.batch.normalize.job.reader;

import com.zipsoon.batch.normalize.repository.NormalizeRepository;
import com.zipsoon.batch.score.model.ScoreType;
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
            List<ScoreType> scoreTypes = normalizeRepository.findAllActiveScoreType();
            iterator = scoreTypes.iterator();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }
}