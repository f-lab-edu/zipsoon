package com.zipsoon.batch.job.writer;

import com.zipsoon.common.domain.EstateSnapshot;
import com.zipsoon.batch.repository.EstateSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstateItemWriter implements ItemWriter<List<EstateSnapshot>> {

    private final EstateSnapshotRepository estateSnapshotRepository;

    @Override
    public void write(Chunk<? extends List<EstateSnapshot>> items) {
        for (List<EstateSnapshot> estateSnapshots : items) {
            try {
                estateSnapshotRepository.saveAll(estateSnapshots);
                log.info("Saved estates: {}", estateSnapshots);
            } catch (DataIntegrityViolationException e) {
                throw new IllegalArgumentException("Failed to save estates due to data integrity violation: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Unexpected failure during batch job execution: " + e.getMessage(), e);
            }
        }
    }
}