package com.zipsoon.batch.estate.job.writer;

import com.zipsoon.common.domain.Estate;
import com.zipsoon.common.domain.EstateSnapshot;
import com.zipsoon.batch.estate.repository.EstateSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstateItemWriter implements ItemWriter<List<EstateSnapshot>> {

    private final EstateSnapshotRepository estateSnapshotRepository;

    @Override
    public void write(Chunk<? extends List<EstateSnapshot>> items) {
        for (List<EstateSnapshot> estateSnapshots : items) {
            try {
                // EstateSnapshot -> Estate 변환
                List<Estate> estates = convertToEstates(estateSnapshots);
                
                // 최신 매물 정보 저장 (estate 테이블에)
                estateSnapshotRepository.saveAllEstates(estates);
                
                // 오래된 데이터는 스냅샷으로 이동
                estateSnapshotRepository.migrateToSnapshot();
                
                log.info("Saved estates: {}", estates.size());
            } catch (DataIntegrityViolationException e) {
                throw new IllegalArgumentException("Failed to save estates due to data integrity violation: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Unexpected failure during batch job execution: " + e.getMessage(), e);
            }
        }
    }
    
    private List<Estate> convertToEstates(List<EstateSnapshot> snapshots) {
        List<Estate> estates = new ArrayList<>();
        
        for (EstateSnapshot snapshot : snapshots) {
            Estate estate = Estate.builder()
                .platformType(snapshot.getPlatformType())
                .platformId(snapshot.getPlatformId())
                .rawData(snapshot.getRawData())
                .estateName(snapshot.getEstateName())
                .estateType(snapshot.getEstateType())
                .tradeType(snapshot.getTradeType())
                .price(snapshot.getPrice())
                .rentPrice(snapshot.getRentPrice())
                .areaMeter(snapshot.getAreaMeter())
                .areaPyeong(snapshot.getAreaPyeong())
                .location(snapshot.getLocation())
                .address(snapshot.getAddress())
                .tags(snapshot.getTags())
                .dongCode(snapshot.getDongCode())
                .createdAt(snapshot.getCreatedAt())
                .imageUrls(snapshot.getImageUrls())
                .build();
            
            estates.add(estate);
        }
        
        return estates;
    }
}