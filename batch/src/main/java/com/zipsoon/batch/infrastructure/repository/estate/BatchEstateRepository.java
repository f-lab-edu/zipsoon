package com.zipsoon.batch.infrastructure.repository.estate;

import com.zipsoon.batch.infrastructure.mapper.estate.BatchEstateMapper;
import com.zipsoon.common.domain.Estate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BatchEstateRepository {
    private final BatchEstateMapper mapper;

    public void saveAll(List<Estate> estates) {
        if (estates == null || estates.isEmpty()) {
            return;
        }
        mapper.insertAll(estates);
    }

    // 최신 부동산 데이터 조회
    public List<Estate> findAll() {
        return mapper.selectAll();
    }
}
