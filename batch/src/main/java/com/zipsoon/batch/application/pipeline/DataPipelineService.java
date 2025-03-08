package com.zipsoon.batch.application.pipeline;

import com.zipsoon.batch.application.pipeline.step.*;
import com.zipsoon.batch.job.migration.DatabaseInitJobRunner;
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
    private final DatabaseInitJobRunner databaseInitJobRunner;
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
        
        log.info("[BATCH:INIT] 파이프라인 단계 등록 완료: {}", stepMap.keySet());
    }

    /**
     * 전체 데이터 파이프라인 실행
     * 모든 단계를 순차적으로 실행하며, 각 단계의 실패는 후속 단계에 영향을 주지 않음
     */
    public void runFullPipeline() {
        log.info("[BATCH:JOB-START] 전체 데이터 파이프라인 실행 시작");
        
        try {
            log.info("[BATCH:JOB-STEP] 데이터베이스 초기화 작업 실행");
            databaseInitJobRunner.run();
            
            runPipelineSteps(stepMap.values());
            
        } catch (Exception e) {
            log.error("[BATCH:JOB-ERR] 데이터베이스 마이그레이션 오류: {}", e.getMessage(), e);
        }
        
        log.info("[BATCH:JOB-END] 전체 데이터 파이프라인 실행 완료");
    }

    /**
     * 특정 단계부터 파이프라인 실행
     * @param startStepName 시작할 단계 이름
     */
    public void runFromStep(String startStepName) {
        log.info("[BATCH:JOB-START] 파이프라인 부분 실행 - 시작 단계: {}", startStepName);

        boolean startFound = false;
        List<PipelineStep> stepsToRun = new ArrayList<>();

        for (PipelineStep step : stepMap.values()) {
            if (startFound || step.getStepName().equals(startStepName)) {
                startFound = true;
                stepsToRun.add(step);
            }
        }

        if (!startFound) {
            log.error("[BATCH:JOB-ERR] 지정된 단계를 찾을 수 없음: {}", startStepName);
            return;
        }

        log.info("[BATCH:JOB-PARAM] 실행할 단계 목록: {}", 
                stepsToRun.stream().map(PipelineStep::getStepName).toList());
        
        runPipelineSteps(stepsToRun);
        
        log.info("[BATCH:JOB-END] 파이프라인 부분 실행 완료 - 시작 단계: {}", startStepName);
    }
    
    /**
     * 지정된 단계들을 순차적으로 실행
     * @param steps 실행할 단계 목록
     */
    private void runPipelineSteps(Iterable<PipelineStep> steps) {
        log.info("[BATCH:PIPELINE-START] 파이프라인 실행 시작");
        
        List<String> succeededSteps = new ArrayList<>();
        List<String> failedSteps = new ArrayList<>();
        Map<String, Long> executionTimes = new LinkedHashMap<>();
        long pipelineStartTime = System.currentTimeMillis();

        for (PipelineStep step : steps) {
            String stepName = step.getStepName();
            
            // 단계 시작 로깅은 각 단계 구현체에 위임
            step.logStepStart();
            
            // 단계 실행 및 시간 측정
            long startTime = System.currentTimeMillis();
            boolean success = step.execute();
            long executionTime = System.currentTimeMillis() - startTime;
            executionTimes.put(stepName, executionTime);
            
            // 단계 종료 로깅은 각 단계 구현체에 위임
            step.logStepEnd(success, executionTime);
            
            if (success) {
                succeededSteps.add(stepName);
            } else {
                failedSteps.add(stepName);
            }
        }
        
        long totalExecutionTime = System.currentTimeMillis() - pipelineStartTime;
        
        // 파이프라인 실행 결과 상세 요약
        log.info("[BATCH:PIPELINE-SUMMARY] 총 실행 시간: {}ms", totalExecutionTime);
        log.info("[BATCH:PIPELINE-SUMMARY] 성공한 단계: {}", succeededSteps);
        log.info("[BATCH:PIPELINE-SUMMARY] 실패한 단계: {}", failedSteps);
        log.info("[BATCH:PIPELINE-SUMMARY] 단계별 실행 시간: {}", executionTimes);
        
        log.info("[BATCH:PIPELINE-END] 파이프라인 실행 완료 - 상태: {}", 
                failedSteps.isEmpty() ? "성공" : "일부 실패");
    }
    
    /**
     * 파이프라인 부분 실행 - 매물 수집부터 시작
     */
    public void runFromEstateCollection() {
        log.info("[BATCH:JOB-START] 파이프라인 부분 실행 - 매물 수집부터 시작");
        
        List<String> stepsToRun = stepMap.values().stream()
            .map(PipelineStep::getStepName)
            .toList();
        log.info("[BATCH:JOB-PARAM] 실행할 단계 목록: {}", stepsToRun);
        
        runPipelineSteps(stepMap.values());
        
        log.info("[BATCH:JOB-END] 파이프라인 부분 실행 완료 - 매물 수집부터");
    }
    
    /**
     * 파이프라인 부분 실행 - 소스 데이터 수집부터 시작
     */
    public void runFromSourceCollection() {
        log.info("[BATCH:JOB-START] 파이프라인 부분 실행 - 소스 데이터 수집부터 시작");
        
        // 소스 수집부터 이후 모든 단계 실행
        List<PipelineStep> steps = new ArrayList<>();
        steps.add(sourceCollectionStep);
        steps.add(scoreCalculationStep);
        steps.add(normalizationStep);
        
        List<String> stepsToRun = steps.stream()
            .map(PipelineStep::getStepName)
            .toList();
        log.info("[BATCH:JOB-PARAM] 실행할 단계 목록: {}", stepsToRun);
        
        runPipelineSteps(steps);
        
        log.info("[BATCH:JOB-END] 파이프라인 부분 실행 완료 - 소스 수집부터");
    }
    
    /**
     * 파이프라인 부분 실행 - 점수 계산부터 시작
     */
    public void runFromScoreCalculation() {
        log.info("[BATCH:JOB-START] 파이프라인 부분 실행 - 점수 계산부터 시작");
        
        // 점수 계산부터 이후 모든 단계 실행
        List<PipelineStep> steps = new ArrayList<>();
        steps.add(scoreCalculationStep);
        steps.add(normalizationStep);
        
        List<String> stepsToRun = steps.stream()
            .map(PipelineStep::getStepName)
            .toList();
        log.info("[BATCH:JOB-PARAM] 실행할 단계 목록: {}", stepsToRun);
        
        runPipelineSteps(steps);
        
        log.info("[BATCH:JOB-END] 파이프라인 부분 실행 완료 - 점수 계산부터");
    }
    
    /**
     * 파이프라인 부분 실행 - 정규화만 실행
     */
    public void runNormalizationOnly() {
        log.info("[BATCH:JOB-START] 파이프라인 부분 실행 - 정규화 단계만 실행");
        
        // 정규화 단계만 실행
        List<PipelineStep> steps = new ArrayList<>();
        steps.add(normalizationStep);
        
        List<String> stepsToRun = steps.stream()
            .map(PipelineStep::getStepName)
            .toList();
        log.info("[BATCH:JOB-PARAM] 실행할 단계: {}", stepsToRun);
        
        runPipelineSteps(steps);
        
        log.info("[BATCH:JOB-END] 파이프라인 부분 실행 완료 - 정규화만 실행");
    }
    
}