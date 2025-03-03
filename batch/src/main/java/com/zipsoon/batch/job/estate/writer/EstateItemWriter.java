package com.zipsoon.batch.estate.job.writer;

import com.zipsoon.batch.infrastructure.repository.estate.BatchEstateRepository;
import com.zipsoon.common.domain.Estate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstateItemWriter implements ItemWriter<List<Estate>> {

    private final BatchEstateRepository batchEstateRepository;

    @Override
    public void write(Chunk<? extends List<Estate>> chunk) {
        try {
            List<Estate> estates = chunk.getItems().stream()
                .flatMap(List::stream)
                .toList();

            // 최신 매물 정보 저장 (estate 테이블에)
            batchEstateRepository.saveAllEstates(estates);

            // 오래된 데이터는 스냅샷으로 이동
            batchEstateRepository.migrateToSnapshot();

            log.info("Saved estates: {}", estates.size());
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Failed to save estates due to data integrity violation: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected failure during batch job execution: " + e.getMessage(), e);
        }
    }
}