package com.zipsoon.batch.job.writer;

import com.zipsoon.batch.exception.BatchJobFailureException;
import com.zipsoon.common.domain.PropertySnapshot;
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
public class PropertyItemWriter implements ItemWriter<List<PropertySnapshot>> {

    private final PropertySnapshotRepository propertySnapshotRepository;

    @Override
    public void write(Chunk<? extends List<PropertySnapshot>> items) {
        for (List<PropertySnapshot> propertySnapshots: items) {
            try {
                propertySnapshotRepository.saveAll(propertySnapshots);
                log.info("Saved properties: {}", propertySnapshots);
            } catch (DataIntegrityViolationException e) {
                throw new InvalidValueException(
                    ErrorCode.RESOURCE_CONFLICT,
                    "Failed to save properties due to data integrity violation",
                    Map.of("failedItems", propertySnapshots)
                );
            } catch (Exception e) {
                throw new InvalidValueException(
                    ErrorCode.BATCH_JOB_FAILED,
                    "Unexpected failure during batch job execution"
                );
            }
        }
    }
}