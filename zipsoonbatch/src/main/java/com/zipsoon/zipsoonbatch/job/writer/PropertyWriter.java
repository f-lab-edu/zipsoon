package com.zipsoon.zipsoonbatch.job.writer;

import com.zipsoon.zipsoonbatch.domain.Property;
import com.zipsoon.zipsoonbatch.domain.PropertyHistory;
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
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class PropertyWriter implements ItemWriter<Property> {
    private final PropertyRepository propertyRepository;
    private final PropertyHistoryRepository propertyHistoryRepository;
    private final Javers javers = JaversBuilder.javers().build();

    @Override
    public void write(Chunk<? extends Property> items) {
        log.info("Writing {} properties", items.size());

        for (Property newProp : items) {
            try {
                newProp.setCreatedAt(LocalDateTime.now());
                propertyRepository.save(newProp);
            } catch (DuplicateKeyException e) {
                Property oldProp = propertyRepository.findByPlatformAndId(
                    newProp.getPlatformType().name(),
                    newProp.getPlatformId()
                ).get(); // 반드시 존재
                Property updatedProp = compareAndUpdateProperty(oldProp, newProp);
                propertyRepository.updateLastCheckedById(updatedProp.getId(), LocalDateTime.now());
            }
        }

    }

    private Property compareAndUpdateProperty(Property oldProp, Property newProp) {
        newProp.setLastChecked(LocalDateTime.now());

        Diff diff = javers.compare(oldProp, newProp);
        if (!diff.hasChanges()) return newProp;

        diff.getChanges().stream()
            .filter(ValueChange.class::isInstance)
            .map(ValueChange.class::cast)
            .map(valueChange -> PropertyHistory.builder()
                    .propertyId(oldProp.getId())
                    .changeType(valueChange.getPropertyName())
                    .beforeValue(valueChange.getLeft() != null ? valueChange.getLeft().toString() : null)
                    .afterValue(valueChange.getRight() != null ? valueChange.getRight().toString() : null)
                    .createdAt(LocalDateTime.now())
                    .build())
            .forEach(propertyHistoryRepository::save);

        newProp.setUpdatedAt(LocalDateTime.now());
        return newProp;
    }

}