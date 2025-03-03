package com.zipsoon.batch.application.pipeline.step;

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
}