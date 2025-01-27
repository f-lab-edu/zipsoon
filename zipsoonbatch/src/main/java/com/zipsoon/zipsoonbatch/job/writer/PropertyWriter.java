package com.zipsoon.zipsoonbatch.job.writer;

import com.zipsoon.zipsoonbatch.domain.Property;
import com.zipsoon.zipsoonbatch.domain.PropertyHistory;
import com.zipsoon.zipsoonbatch.domain.UpsertResult;
import com.zipsoon.zipsoonbatch.repository.PropertyHistoryRepository;
import com.zipsoon.zipsoonbatch.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.ValueChange;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class PropertyWriter implements ItemWriter<Property> {
    private final PropertyRepository propertyRepository;
    private final PropertyHistoryRepository propertyHistoryRepository;
    private final Javers javers = JaversBuilder.javers().build();

    @Override
    public void write(Chunk<? extends Property> items) {
        log.info("Writing {} properties", items.size());

        List<? extends Property> properties = items.getItems();
        for (Property newProp : properties) {
            UpsertResult result = propertyRepository.upsert(newProp);

            if ("UPDATE".equals(result.getOperation())) {
                Property oldProp = propertyRepository.findById(result.getId()).get();
                recordChanges(oldProp, newProp);
            }
        }
    }

    private void recordChanges(Property oldProp, Property newProp) {
        Diff diff = javers.compare(oldProp, newProp);
        if (!diff.hasChanges()) return;

        LocalDateTime now = LocalDateTime.now();
        newProp.setUpdatedAt(now);
        propertyRepository.update(newProp);

        diff.getChanges().stream()
            .filter(ValueChange.class::isInstance)
            .map(ValueChange.class::cast)
            .map(valueChange -> PropertyHistory.builder()
                    .propertyId(oldProp.getId())
                    .changeType(valueChange.getPropertyName())
                    .beforeValue(Optional.ofNullable(valueChange.getLeft()).map(Object::toString).orElse(null))
                    .afterValue(Optional.ofNullable(valueChange.getRight()).map(Object::toString).orElse(null))
                    .createdAt(now)
                    .build())
            .forEach(propertyHistoryRepository::save);
    }

}