package com.zipsoon.batch.application.pipeline.step;

import com.zipsoon.batch.job.estate.EstateJobRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 부동산 매물 데이터 수집 단계
 * 네이버 부동산 API를 통해 매물 정보를 수집하는 작업 실행
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EstateCollectionStep implements PipelineStep {
    private final EstateJobRunner estateJobRunner;
    private static final String STEP_NAME = "ESTATE_COLLECTION";
    
    /**
     * 부동산 매물 수집 작업 실행
     * 법정동 코드 기반으로 매물 데이터를 수집하여 저장
     * @return 실행 성공 여부
     */
    @Override
    public boolean execute() {
        log.info("Executing estate collection step");
        try {
            estateJobRunner.run();
            log.info("Estate collection step completed successfully");
            return true;
        } catch (Exception e) {
            log.error("Estate collection step failed: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getStepName() {
        return STEP_NAME;
    }
}