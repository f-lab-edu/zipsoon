package com.zipsoon.zipsoonbatch.repository;

import com.zipsoon.zipsoonbatch.domain.PropertyHistory;
import com.zipsoon.zipsoonbatch.repository.mapper.PropertyHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PropertyHistoryRepository {
    private final PropertyHistoryMapper propertyHistoryMapper;

    public void save(PropertyHistory history) {
        propertyHistoryMapper.save(history);
    }
}