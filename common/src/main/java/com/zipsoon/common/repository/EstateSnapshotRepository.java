package com.zipsoon.common.repository;

import com.zipsoon.common.domain.EstateSnapshot;
import com.zipsoon.common.mapper.PropertySnapshotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PropertySnapshotRepository {

    private final PropertySnapshotMapper propertySnapshotMapper;

    public void saveAll(List<EstateSnapshot> estateSnapshots) {
        propertySnapshotMapper.insertPropertySnapshots(estateSnapshots);
    }

    public List<EstateSnapshot> findAll() {
        return propertySnapshotMapper.selectAllPropertySnapshots();
    }
}
