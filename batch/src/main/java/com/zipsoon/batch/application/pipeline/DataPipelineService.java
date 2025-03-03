package com.zipsoon.batch.application.pipeline;

import com.zipsoon.batch.application.pipeline.step.*;
import com.zipsoon.batch.job.migration.DatabaseMigrationJobRunner;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 전체 데이터 파이프라인 흐름을 관리하는 서비스
 * 스냅샷 이동 -> 부동산 매물 수집 -> 점수 소스 데이터 수집 -> 점수 계산 -> 정규화 순서로 진행
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataPipelineService {
    private final DatabaseMigrationJobRunner databaseMigrationJobRunner;
    private final EstateCollectionStep estateCollectionStep;
    private final SourceCollectionStep sourceCollectionStep;
    private final ScoreCalculationStep scoreCalculationStep;
    private final NormalizationStep normalizationStep;
    
    private final Map<String, PipelineStep> stepMap = new LinkedHashMap<>();
    
    @PostConstruct
    public void init() {
        stepMap.put(estateCollectionStep.getStepName(), estateCollectionStep);
        stepMap.put(sourceCollectionStep.getStepName(), sourceCollectionStep);
        stepMap.put(scoreCalculationStep.getStepName(), scoreCalculationStep);
        stepMap.put(normalizationStep.getStepName(), normalizationStep);
        
        log.info("Pipeline steps registered: {}", stepMap.keySet());
    }

    /**
     * 전체 데이터 파이프라인 실행
     * 모든 단계를 순차적으로 실행하며, 각 단계의 실패는 후속 단계에 영향을 주지 않음
     */
    public void runFullPipeline() {
        log.info("Starting full data pipeline execution");
        
        try {
            log.info("Running database migration job first");
            databaseMigrationJobRunner.run();
            
            runPipelineSteps(stepMap.values());
            
        } catch (Exception e) {
            log.error("Error during database migration: {}", e.getMessage(), e);
        }
        
        log.info("Full data pipeline execution completed");
    }

    /**
     * 특정 단계부터 파이프라인 실행
     * @param startStepName 시작할 단계 이름
     */
    public void runFromStep(String startStepName) {
        log.info("Starting pipeline from step: {}", startStepName);

        boolean startFound = false;
        List<PipelineStep> stepsToRun = new ArrayList<>();

        for (PipelineStep step : stepMap.values()) {
            if (startFound || step.getStepName().equals(startStepName)) {
                startFound = true;
                stepsToRun.add(step);
            }
        }

        if (!startFound) {
            log.error("Step not found: {}", startStepName);
            return;
        }

        runPipelineSteps(stepsToRun);
    }
    
    /**
     * 지정된 단계들을 순차적으로 실행
     * @param steps 실행할 단계 목록
     */
    private void runPipelineSteps(Iterable<PipelineStep> steps) {
        List<String> succeededSteps = new ArrayList<>();
        List<String> failedSteps = new ArrayList<>();

        for (PipelineStep step : steps) {
            String stepName = step.getStepName();
            log.info("Executing pipeline step: {}", stepName);
            
            boolean success = step.execute();
            
            if (success) {
                succeededSteps.add(stepName);
            } else {
                failedSteps.add(stepName);
            }
        }
        
        // 파이프라인 실행 결과 요약
        log.info("Pipeline execution summary - Succeeded: {}, Failed: {}", 
                succeededSteps, failedSteps);
    }
    
    /**
     * 파이프라인 부분 실행 - 매물 수집부터 시작
     */
    public void runFromEstateCollection() {
        log.info("Starting pipeline from estate collection");
        runPipelineSteps(stepMap.values());
    }
    
    /**
     * 파이프라인 부분 실행 - 소스 데이터 수집부터 시작
     */
    public void runFromSourceCollection() {
        log.info("Starting pipeline from source collection");
        
        // 소스 수집부터 이후 모든 단계 실행
        List<PipelineStep> steps = new ArrayList<>();
        steps.add(sourceCollectionStep);
        steps.add(scoreCalculationStep);
        steps.add(normalizationStep);
        
        runPipelineSteps(steps);
    }
    
    /**
     * 파이프라인 부분 실행 - 점수 계산부터 시작
     */
    public void runFromScoreCalculation() {
        log.info("Starting pipeline from score calculation");
        
        // 점수 계산부터 이후 모든 단계 실행
        List<PipelineStep> steps = new ArrayList<>();
        steps.add(scoreCalculationStep);
        steps.add(normalizationStep);
        
        runPipelineSteps(steps);
    }
    
    /**
     * 파이프라인 부분 실행 - 정규화만 실행
     */
    public void runNormalizationOnly() {
        log.info("Running normalization step only");
        
        // 정규화 단계만 실행
        List<PipelineStep> steps = new ArrayList<>();
        steps.add(normalizationStep);
        
        runPipelineSteps(steps);
    }
    
}