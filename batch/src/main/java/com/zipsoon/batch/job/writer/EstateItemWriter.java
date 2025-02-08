package com.zipsoon.batch.job.writer;

import com.zipsoon.common.domain.EstateSnapshot;
import com.zipsoon.common.exception.ErrorCode;
import com.zipsoon.common.exception.domain.InvalidValueException;
import com.zipsoon.common.repository.PropertySnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PropertyItemWriter implements ItemWriter<List<EstateSnapshot>> {

    private final PropertySnapshotRepository propertySnapshotRepository;

    @Override
    public void write(Chunk<? extends List<EstateSnapshot>> items) {
        for (List<EstateSnapshot> estateSnapshots : items) {
            try {
                propertySnapshotRepository.saveAll(estateSnapshots);
                log.info("Saved properties: {}", estateSnapshots);
            } catch (DataIntegrityViolationException e) {
                throw new InvalidValueException(
                    ErrorCode.RESOURCE_CONFLICT,
                    "Failed to save properties due to data integrity violation",
                    Map.of("failedItems", estateSnapshots)
                );
            } catch (Exception e) {
                throw new InvalidValueException(
                    ErrorCode.INTERNAL_ERROR,
                    "Unexpected failure during batch job execution"
                );
            }
        }
    }
}