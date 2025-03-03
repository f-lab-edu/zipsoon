package com.zipsoon.batch.application.pipeline.step;

import com.zipsoon.batch.job.source.SourceJobRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 점수 계산에 필요한 소스 데이터 수집 단계
 * 공원, 지하철역 등의 위치 데이터를 수집하는 작업 실행
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SourceCollectionStep implements PipelineStep {
    private final SourceJobRunner sourceJobRunner;
    private static final String STEP_NAME = "SOURCE_COLLECTION";
    
    /**
     * 소스 데이터 수집 작업 실행
     * csv 등 파일로부터 테이블 생성 및 데이터 저장
     * @return 실행 성공 여부
     */
    @Override
    public boolean execute() {
        log.info("Executing source data collection step");
        try {
            sourceJobRunner.run();
            log.info("Source data collection step completed successfully");
            return true;
        } catch (Exception e) {
            log.error("Source data collection step failed: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getStepName() {
        return STEP_NAME;
    }
}