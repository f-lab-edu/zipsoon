package com.zipsoon.batch.repository;

import com.zipsoon.common.domain.EstateSnapshot;
import com.zipsoon.batch.mapper.EstateSnapshotMapper;
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

}
