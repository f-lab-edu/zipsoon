package com.zipsoon.batch.job.estate.writer;

import com.zipsoon.batch.infrastructure.repository.estate.BatchEstateRepository;
import com.zipsoon.common.domain.Estate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 수집된 부동산 매물 데이터를 estate 테이블에 저장하는 Writer
 * 스냅샷 이동 및 테이블 비우기는 별도 Tasklet에서 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EstateItemWriter implements ItemWriter<List<Estate>> {

    private final BatchEstateRepository batchEstateRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends List<Estate>> chunk) {
        try {
            List<Estate> estates = chunk.getItems().stream()
                .flatMap(List::stream)
                .toList();
            
            // 새로운 매물 정보 저장
            log.info("Saving new estates data: {} items", estates.size());
            batchEstateRepository.saveAllEstates(estates);

            log.info("Estate data saved successfully: {} estates", estates.size());
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Failed to save estates due to data integrity violation: " + e.getMessage(), e);
        } catch (Exception e) {
            // 예외 발생 시 트랜잭션이 롤백됨
            log.error("Transaction will be rolled back due to error: {}", e.getMessage());
            throw new RuntimeException("Unexpected failure during batch job execution: " + e.getMessage(), e);
        }
    }
}