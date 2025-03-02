package com.zipsoon.batch.score.job.reader;

import com.zipsoon.batch.estate.repository.EstateSnapshotRepository;
import com.zipsoon.common.domain.Estate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreReader implements ItemReader<Estate> {
    private final EstateSnapshotRepository estateSnapshotRepository;
    private Iterator<Estate> estatesIterator;
    private boolean initialized = false;

    private void initialize() {
        if (!initialized) {
            List<Estate> estates = estateSnapshotRepository.findAllEstates();
            this.estatesIterator = estates.iterator();
            log.info("Loaded {} estates for scoring", estates.size());
            initialized = true;
        }
    }

    @Override
    public Estate read() {
        initialize();

        if (estatesIterator.hasNext()) {
            return estatesIterator.next();
        }
        return null;
    }

}
