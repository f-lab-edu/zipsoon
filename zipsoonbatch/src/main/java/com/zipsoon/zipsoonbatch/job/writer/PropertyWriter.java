package com.zipsoon.zipsoonbatch.job.writer;

import com.zipsoon.zipsoonbatch.domain.Property;
import com.zipsoon.zipsoonbatch.domain.PropertyHistory;
import com.zipsoon.zipsoonbatch.repository.PropertyHistoryRepository;
import com.zipsoon.zipsoonbatch.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class PropertyWriter implements ItemWriter<Property> {
    private final PropertyRepository propertyRepository;
    private final PropertyHistoryRepository propertyHistoryRepository;

    @Override
    public void write(Chunk<? extends Property> items) {
        log.info("Writing {} properties", items.size());

        for (Property property : items) {
            Optional<Property> existingPropertyOpt = propertyRepository.findByPlatformTypeAndPlatformId(
                property.getPlatformType().name(),
                property.getPlatformId()
            );

            if (existingPropertyOpt.isPresent()) {
                // 기존 매물
                Property existingProperty = existingPropertyOpt.get();
                if (hasChanges(existingProperty, property)) {
                    property.setId(existingProperty.getId());
                    property.setCreatedAt(existingProperty.getCreatedAt());
                    property.setUpdatedAt(LocalDateTime.now());
                    propertyRepository.save(property);

                    recordHistory(property.getId(),
                        "UPDATE",
                        propertyToString(existingProperty),
                        propertyToString(property));
                }
            } else {
                // 신규 매물
                property.setCreatedAt(LocalDateTime.now());
                property.setUpdatedAt(LocalDateTime.now());
                propertyRepository.save(property);
            }
        }
    }

    private void recordHistory(Long propertyId, String changeType,
                             String beforeValue, String afterValue) {
        PropertyHistory history = PropertyHistory.builder()
            .propertyId(propertyId)
            .changeType(changeType)
            .beforeValue(beforeValue)
            .afterValue(afterValue)
            .createdAt(LocalDateTime.now())
            .build();

        propertyHistoryRepository.save(history);
    }

    private boolean hasChanges(Property existingProperty, Property newProperty) {
        return !propertyToString(existingProperty).equals(propertyToString(newProperty));
    }

    private String propertyToString(Property property) {
        return String.format("%s|%s|%s|%s|%s|%s",
            property.getPrice(),
            property.getStatus(),
            property.getTradeType(),
            property.getArea(),
            property.getFeatureDescription(),
            property.getPriceChangeState()
        );
    }

}