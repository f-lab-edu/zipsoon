package com.zipsoon.batch.infrastructure.processor.source.collector;

import com.zipsoon.batch.application.service.source.collector.SourceCollector;
import com.zipsoon.batch.infrastructure.processor.source.loader.CsvSourceFileLoader;
import com.zipsoon.batch.infrastructure.processor.source.loader.ResourceUtils;
import com.zipsoon.batch.infrastructure.repository.source.SourceRepository;
import com.zipsoon.batch.infrastructure.util.BatchJobUtils;
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
public class DongCodeSourceCollector implements SourceCollector {
    private final SourceRepository sourceRepository;
    private final CsvSourceFileLoader csvSourceFileLoader;
    private final JobExplorer jobExplorer;

    private static final String TABLE_NAME = "dongcodes";
    private static final String SCHEMA_FILE = "source/sql/dongcode-resource-query.sql";
    private static final String DATA_FILE = "source/data/dongcode-resource-data.csv";
    private static final String JOB_NAME = "sourceJob";

    @Override
    public void create() {
        try {
            sourceRepository.dropTable(TABLE_NAME);
            log.info("기존 법정동코드 테이블 삭제");
            
            String createTableSql = ResourceUtils.toString(SCHEMA_FILE);
            sourceRepository.executeDDL(createTableSql);
            log.info("법정동코드 테이블 생성/업데이트 완료");
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
            log.info("총 {}개의 법정동코드 데이터를 가져왔습니다.", rowsCopied);
        } catch (IOException | SQLException e) {
            log.error("CSV 파일 처리 실패: {}", e.getMessage(), e);
            throw new RuntimeException("데이터 수집 실패", e);
        }
    }

    @Override
    public void preprocess() {
        log.info("법정동코드 데이터 전처리 생략");
    }
    
    @Override
    public boolean wasUpdated() {
        try {
            Path resourcePath = ResourceUtils.getResourcePath(DATA_FILE);

            LocalDateTime fileLastModified = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(Files.getLastModifiedTime(resourcePath).toMillis()),
                ZoneId.systemDefault()
            );
            
            // 유틸리티 클래스 사용
            LocalDateTime lastSuccessTime = BatchJobUtils.getLastSuccessfulJobTime(jobExplorer, JOB_NAME);

            // 유틸리티 클래스를 사용하여 업데이트 필요 여부 확인 및 로깅
            return BatchJobUtils.checkNeedsUpdate(fileLastModified, lastSuccessTime, "법정동코드");
        } catch (Exception e) {
            log.error("파일 변경 확인 중 오류 발생: {}", e.getMessage(), e);
            return true;
        }
    }
}