package com.zipsoon.batch.infrastructure.processor.source.collector;

import com.zipsoon.batch.application.service.source.collector.SourceCollector;
import com.zipsoon.batch.infrastructure.processor.source.loader.CsvSourceFileLoader;
import com.zipsoon.batch.infrastructure.processor.source.loader.ResourceUtils;
import com.zipsoon.batch.infrastructure.repository.source.SourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkSourceCollector implements SourceCollector {
    private final SourceRepository sourceRepository;
    private final CsvSourceFileLoader csvSourceFileLoader;
    private final JobExplorer jobExplorer;

    private static final String TABLE_NAME = "parks";
    private static final String SCHEMA_FILE = "source/sql/park-score-resource-query.sql";
    private static final String DATA_FILE = "source/data/park-score-resource-data.csv";
    private static final String JOB_NAME = "sourceJob";

    @Override
    public void create() {
        try {
            String createTableSql = ResourceUtils.toString(SCHEMA_FILE);
            sourceRepository.executeDDL(createTableSql);
            log.info("공원 테이블 생성/업데이트 완료");
        } catch (IOException e) {
            log.error("SQL 파일 읽기 실패: {}", e.getMessage(), e);
            throw new RuntimeException("SQL 파일 읽기 실패", e);
        }
    }

    @Override
    public void collect() {
        try {
            // 기존 데이터 초기화
            sourceRepository.truncateTable(TABLE_NAME);
            
            Reader dataFileReader = ResourceUtils.toReader(DATA_FILE);
            int rowsCopied = csvSourceFileLoader.load(dataFileReader, TABLE_NAME);
            log.info("총 {}개의 공원 데이터를 가져왔습니다.", rowsCopied);
        } catch (IOException | SQLException e) {
            log.error("CSV 파일 처리 실패: {}", e.getMessage(), e);
            throw new RuntimeException("데이터 수집 실패", e);
        }
    }

    @Override
    public void preprocess() {
        try {
            sourceRepository.addLocationColumn(TABLE_NAME);
            log.info("공원 테이블에 location 컬럼을 추가했습니다.");
            int updatedRows = sourceRepository.updateLocationCoordinates(TABLE_NAME);
            log.info("{}개의 공원 위치 데이터를 업데이트했습니다.", updatedRows);
        } catch (Exception e) {
            log.error("전처리 작업 실패: {}", e.getMessage(), e);
            throw new RuntimeException("전처리 실패", e);
        }
    }
    
    @Override
    public boolean wasUpdated() {
        try {
            Path resourcePath = ResourceUtils.getResourcePath(DATA_FILE);

            long fileLastModified = Files.getLastModifiedTime(resourcePath).toMillis();
            
            LocalDateTime lastSuccessTime = getLastSuccessfulJobTime();
            
            boolean needsUpdate = lastSuccessTime == null ||
                  fileLastModified > lastSuccessTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            if (needsUpdate) {
                log.info("소스 파일이 변경되어 데이터를 업데이트합니다. (파일 수정: {})", 
                        Instant.ofEpochMilli(fileLastModified));
            } else {
                log.info("소스 파일에 변경이 없어 데이터 업데이트를 건너뜁니다. (마지막 배치: {})", 
                        lastSuccessTime);
            }
            
            return needsUpdate;
        } catch (Exception e) {
            log.error("파일 변경 확인 중 오류 발생: {}", e.getMessage(), e);
            return true;
        }
    }
    
    private LocalDateTime getLastSuccessfulJobTime() {
        try {
            // JobExplorer를 통해 작업 인스턴스 조회 (최대 100개)
            List<JobInstance> jobInstances = jobExplorer.getJobInstances(JOB_NAME, 0, 100);
            
            if (jobInstances.isEmpty()) {
                log.info("이전 작업 인스턴스가 없습니다: {}", JOB_NAME);
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
}