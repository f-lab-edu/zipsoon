package com.zipsoon.batch.application.pipeline.step;

import com.zipsoon.batch.job.normalize.NormalizeJobRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 점수 정규화 단계
 * 계산된 원시 점수를 0-10 사이의 값으로 정규화하는 작업 실행
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NormalizationStep implements PipelineStep {
    private final NormalizeJobRunner normalizeJobRunner;
    private static final String STEP_NAME = "NORMALIZATION";
    
    /**
     * 점수 정규화 작업 실행
     * 원시 점수를 표준화된 범위로 변환하고 매물 간 상대적 순위 부여
     * @return 실행 성공 여부
     */
    @Override
    public boolean execute() {
        log.info("Executing score normalization step");
        try {
            normalizeJobRunner.run();
            log.info("Score normalization step completed successfully");
            return true;
        } catch (Exception e) {
            log.error("Score normalization step failed: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getStepName() {
        return STEP_NAME;
    }
}