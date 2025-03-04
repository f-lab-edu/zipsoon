package com.zipsoon.batch.infrastructure.processor.source.collector;

import com.zipsoon.batch.application.service.source.collector.SourceCollector;
import com.zipsoon.batch.infrastructure.processor.source.loader.CsvSourceFileLoader;
import com.zipsoon.batch.infrastructure.processor.source.loader.ResourceUtils;
import com.zipsoon.batch.infrastructure.repository.source.SourceRepository;
import com.zipsoon.batch.infrastructure.util.BatchJobUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            sourceRepository.dropTable(TABLE_NAME);
            log.info("기존 공원 테이블 삭제");
            
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

            LocalDateTime fileLastModified = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(Files.getLastModifiedTime(resourcePath).toMillis()),
                ZoneId.systemDefault()
            );
            LocalDateTime lastSuccessTime = BatchJobUtils.getLastSuccessfulJobTime(jobExplorer, JOB_NAME);

            return BatchJobUtils.checkNeedsUpdate(fileLastModified, lastSuccessTime, "공원");
        } catch (Exception e) {
            log.error("파일 변경 확인 중 오류 발생: {}", e.getMessage(), e);
            return true;
        }
    }
}