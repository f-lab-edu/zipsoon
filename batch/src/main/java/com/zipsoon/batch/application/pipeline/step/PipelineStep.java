package com.zipsoon.batch.application.pipeline.step;

import org.slf4j.LoggerFactory;

/**
 * 데이터 파이프라인의 각 단계를 정의하는 인터페이스
 * 모든 파이프라인 단계(Step)는 이 인터페이스를 구현해야 함
 */
public interface PipelineStep {
    /**
     * 파이프라인 단계 실행
     * @return 실행 성공 여부
     */
    boolean execute();
    
    /**
     * 단계 이름 반환
     * @return 파이프라인 단계 식별자
     */
    String getStepName();
    
    /**
     * 단계 시작 시 로깅
     */
    default void logStepStart() {
        LoggerFactory.getLogger(this.getClass()).info(
            "[BATCH:STEP-START] {} - 실행 시작", getStepName());
    }
    
    /**
     * 단계 종료 시 로깅
     * @param success 성공 여부
     * @param executionTimeMs 실행 시간(ms)
     */
    default void logStepEnd(boolean success, long executionTimeMs) {
        LoggerFactory.getLogger(this.getClass()).info(
            "[BATCH:STEP-END] {} - {} - 소요시간: {}ms", 
            getStepName(), 
            success ? "성공" : "실패",
            executionTimeMs);
    }
}