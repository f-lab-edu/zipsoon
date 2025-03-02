package com.zipsoon.batch.estate.repository;

import com.zipsoon.common.domain.Estate;
import com.zipsoon.common.domain.EstateSnapshot;
import com.zipsoon.batch.estate.mapper.EstateSnapshotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EstateSnapshotRepository {
    private final EstateSnapshotMapper estateSnapshotMapper;

    // 기존 스냅샷 메서드
    public void saveAllSnapshots(List<EstateSnapshot> estateSnapshots) {
        estateSnapshotMapper.insertEstateSnapshots(estateSnapshots);
    }

    public List<EstateSnapshot> findAllLatestSnapshots() {
        return estateSnapshotMapper.selectLatestAll();
    }
    
    // 새로운 estate 테이블 관련 메서드
    public void saveAllEstates(List<Estate> estates) {
        estateSnapshotMapper.insertEstates(estates);
    }
    
    // 오래된 데이터 스냅샷으로 이동
    public void migrateToSnapshot() {
        estateSnapshotMapper.migrateToSnapshot();
    }
    
    // 최신 부동산 데이터 조회
    public List<Estate> findAllEstates() {
        return estateSnapshotMapper.selectAllEstates();
    }
}
