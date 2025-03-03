package com.zipsoon.batch.application.pipeline.step;

import com.zipsoon.batch.job.score.ScoreJobRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 매물별 점수 계산 단계
 * 부동산 매물과 소스 데이터를 기반으로 점수를 산출하는 작업 실행
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScoreCalculationStep implements PipelineStep {
    private final ScoreJobRunner scoreJobRunner;
    private static final String STEP_NAME = "SCORE_CALCULATION";
    
    /**
     * 점수 계산 작업 실행
     * 각 매물의 위치와 주변 시설을 분석하여 원시 점수를 계산
     * @return 실행 성공 여부
     */
    @Override
    public boolean execute() {
        log.info("Executing score calculation step");
        try {
            scoreJobRunner.run();
            log.info("Score calculation step completed successfully");
            return true;
        } catch (Exception e) {
            log.error("Score calculation step failed: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getStepName() {
        return STEP_NAME;
    }
}