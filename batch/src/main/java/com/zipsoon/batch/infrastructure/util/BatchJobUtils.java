package com.zipsoon.batch.infrastructure.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 배치 작업 관련 유틸리티 메서드를 제공하는 클래스
 */
@Slf4j
public class BatchJobUtils {

    private BatchJobUtils() {
        // 인스턴스화 방지
    }

    /**
     * 마지막으로 성공한 작업 실행 시간을 가져옵니다.
     * 
     * @param jobExplorer 작업 탐색기
     * @param jobName 작업 이름
     * @return 마지막 성공 시간, 없으면 null
     */
    public static LocalDateTime getLastSuccessfulJobTime(JobExplorer jobExplorer, String jobName) {
        try {
            // JobExplorer를 통해 작업 인스턴스 조회 (최대 100개)
            List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, 0, 100);
            
            if (jobInstances.isEmpty()) {
                log.info("이전 작업 인스턴스가 없습니다: {}", jobName);
                return null;
            }
            
            // 각 작업 인스턴스의 실행 정보 조회 및 성공한 마지막 실행 시간 찾기
            return jobInstances.stream()
                .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
                .filter(execution -> "COMPLETED".equals(execution.getExitStatus().getExitCode()))
                .map(JobExecution::getEndTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        } catch (Exception e) {
            log.error("배치 메타데이터 조회 실패: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 파일의 업데이트 필요 여부를 확인하고 로깅합니다.
     * 
     * @param fileLastModified 파일 마지막 수정 시간
     * @param lastSuccessTime 마지막 성공 시간
     * @param sourceName 소스 이름
     * @return 업데이트 필요 여부 (true: 필요, false: 불필요)
     */
    public static boolean checkNeedsUpdate(LocalDateTime fileLastModified, 
                                          LocalDateTime lastSuccessTime, 
                                          String sourceName) {
        boolean needsUpdate = lastSuccessTime == null || fileLastModified.isAfter(lastSuccessTime);
        
        if (needsUpdate) {
            log.info("{} 소스 파일이 변경되어 데이터를 업데이트합니다. (파일 수정: {})", 
                    sourceName, fileLastModified);
            log.debug("파일수정시간: {}, 마지막성공시간: {}", fileLastModified, lastSuccessTime);
        } else {
            log.info("{} 소스 파일에 변경이 없어 데이터 업데이트를 건너뜁니다. (마지막 배치: {})", 
                    sourceName, lastSuccessTime);
            log.debug("파일수정시간: {}, 마지막성공시간: {}", fileLastModified, lastSuccessTime);
        }
        
        return needsUpdate;
    }
}