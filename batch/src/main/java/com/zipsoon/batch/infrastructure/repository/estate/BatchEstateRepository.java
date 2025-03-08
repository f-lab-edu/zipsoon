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

    // 새로운 estate 테이블 관련 메서드
    public void saveAll(List<Estate> estates) {
        if (estates == null || estates.isEmpty()) {
            // 빈 리스트인 경우 저장하지 않고 로그만 남김
            return;
        }
        mapper.insertAll(estates);
    }
    
    // 모든 데이터 스냅샷으로 이동
    public void migrateToSnapshot() {
        mapper.migrateToSnapshot();
    }
    
    // estate 테이블 비우기
    public void truncateEstateTable() {
        mapper.truncateEstateTable();
    }
    
    // 최신 부동산 데이터 조회
    public List<Estate> findAll() {
        return mapper.selectAll();
    }
}
