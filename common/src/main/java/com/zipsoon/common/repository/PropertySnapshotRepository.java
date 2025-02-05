package com.zipsoon.common.repository;

import com.zipsoon.common.domain.PropertySnapshot;
import com.zipsoon.common.mapper.PropertySnapshotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PropertySnapshotRepository {

    private final PropertySnapshotMapper propertySnapshotMapper;

    public void saveAll(List<PropertySnapshot> propertySnapshots) {
        propertySnapshotMapper.insertPropertySnapshots(propertySnapshots);
    }

    public List<PropertySnapshot> findAll() {
        return propertySnapshotMapper.selectAllPropertySnapshots();
    }
}
