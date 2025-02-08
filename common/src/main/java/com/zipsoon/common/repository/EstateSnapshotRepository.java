package com.zipsoon.common.repository;

import com.zipsoon.common.domain.EstateSnapshot;
import com.zipsoon.common.mapper.EstateSnapshotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EstateSnapshotRepository {

    private final EstateSnapshotMapper estateSnapshotMapper;

    public void saveAll(List<EstateSnapshot> estateSnapshots) {
        estateSnapshotMapper.insertEstateSnapshots(estateSnapshots);
    }

    public List<EstateSnapshot> findAll() {
        return estateSnapshotMapper.selectAllEstateSnapshots();
    }
}
