package com.zipsoon.batch.job;

import com.zipsoon.batch.application.pipeline.DataPipelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 통합 배치 작업 실행기
 * 애플리케이션 시작 시 자동으로 실행되며, 전체 데이터 파이프라인 진행
 * 모든 작업이 완료되면 애플리케이션 자동 종료
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobRunner implements CommandLineRunner, ExitCodeGenerator {
    private final DataPipelineService pipelineService;
    private final ConfigurableApplicationContext context;
    private int exitCode = 0;
    
    @Override
    public void run(String... args) {
        log.info("Starting batch job pipeline execution");
        
        try {
            // 커맨드 라인 인자가 있는 경우 특정 단계부터 실행
            if (args.length > 0) {
                String startStep = args[0].toUpperCase();
                runFrom(startStep);
            } else {
                // 인자가 없는 경우 전체 파이프라인 실행
                pipelineService.runFullPipeline();
            }
            
            log.info("Batch job pipeline execution completed successfully");
        } catch (Exception e) {
            log.error("Error during batch job pipeline execution", e);
            exitCode = 1;
        } finally {
            // 애플리케이션 종료
            log.info("Exiting application with exit code: {}", exitCode);
            SpringApplication.exit(context, () -> exitCode);
        }
    }
    
    /**
     * 특정 단계부터 파이프라인 실행
     * @param startPoint 시작 지점 (estate, source, score, normalize)
     */
    public void runFrom(String startPoint) {
        try {
            switch (startPoint.toLowerCase()) {
                case "estate":
                    pipelineService.runFromEstateCollection();
                    break;
                case "source":
                    pipelineService.runFromSourceCollection();
                    break;
                case "score":
                    pipelineService.runFromScoreCalculation();
                    break;
                case "normalize":
                    pipelineService.runNormalizationOnly();
                    break;
                default:
                    // 직접 단계 이름으로 실행 시도
                    pipelineService.runFromStep(startPoint);
                    break;
            }
        } catch (Exception e) {
            log.error("Error during batch job pipeline execution from {}", startPoint, e);
            exitCode = 1;
        }
    }
    
    @Override
    public int getExitCode() {
        return exitCode;
    }
}