package com.zipsoon.batch.job.writer;

import com.zipsoon.common.domain.PropertySnapshot;
import com.zipsoon.common.repository.PropertySnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

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
            } catch (Exception e) {
                log.error("Failed to save property snapshots: {}", propertySnapshots, e);
            }
        }
    }
}