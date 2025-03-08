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
            log.info("[BATCH:STEP-WRITER] 새로운 매물 데이터 저장 시작: 총 {}개", estates.size());
            
            // 빈 리스트인 경우 저장 작업 생략
            if (estates.isEmpty()) {
                log.info("[BATCH:STEP-WRITER] 저장할 매물 없음, 데이터베이스 작업 생략");
                return;
            }
            
            batchEstateRepository.saveAll(estates);
            log.info("[BATCH:STEP-WRITER] 매물 데이터 저장 완료: {}개", estates.size());
        } catch (DataIntegrityViolationException e) {
            log.error("[BATCH:STEP-ERR] 매물 데이터 무결성 위반 오류: {}", e.getMessage());
            throw new IllegalArgumentException("데이터 무결성 위반으로 매물 저장 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            // 예외 발생 시 트랜잭션이 롤백됨
            log.error("[BATCH:STEP-ERR] 오류로 인한 트랜잭션 롤백: {}", e.getMessage());
            throw new RuntimeException("배치 작업 실행 중 예상치 못한 오류 발생: " + e.getMessage(), e);
        }
    }
}