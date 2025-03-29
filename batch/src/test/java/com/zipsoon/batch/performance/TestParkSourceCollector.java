package com.zipsoon.batch.performance;

import com.zipsoon.batch.application.service.source.collector.SourceCollector;
import com.zipsoon.batch.infrastructure.processor.source.loader.CsvSourceFileLoader;
import com.zipsoon.batch.infrastructure.processor.source.loader.TestResourceUtils;
import com.zipsoon.batch.infrastructure.repository.source.SourceRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.explore.JobExplorer;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * 테스트용 ParkSourceCollector - 실제와 동일한 로직을 수행하지만 TestResourceUtils를 사용
 */
@Slf4j
public class TestParkSourceCollector implements SourceCollector {
    private final SourceRepository sourceRepository;
    private final CsvSourceFileLoader csvSourceFileLoader;
    private final JobExplorer jobExplorer;
    private final String collectorId;
    
    @Getter
    @Setter
    private boolean needsUpdate = true;

    private static final String TABLE_NAME = "parks";
    private static final String SCHEMA_FILE = "sql/park-score-resource-query.sql";
    private static final String DATA_FILE = "data/park-score-resource-data.csv";
    private static final String JOB_NAME = "sourceJob";

    public TestParkSourceCollector(SourceRepository sourceRepository, 
                                  CsvSourceFileLoader csvSourceFileLoader, 
                                  JobExplorer jobExplorer,
                                  String collectorId) {
        this.sourceRepository = sourceRepository;
        this.csvSourceFileLoader = csvSourceFileLoader;
        this.jobExplorer = jobExplorer;
        this.collectorId = collectorId;
    }

    @Override
    public void create() {
        try {
            // TestSourceRepository를 사용하는 경우 간소화된 테이블 생성
            if (sourceRepository instanceof TestSourceRepository) {
                ((TestSourceRepository) sourceRepository).createSimplifiedParkTable();
                log.info("[{}] 테스트용 간소화된 공원 테이블 생성 완료", collectorId);
            } else {
                // 일반적인 경우 SQL 파일 사용
                sourceRepository.dropTable(TABLE_NAME);
                log.info("[{}] 기존 테스트 공원 테이블 삭제", collectorId);
                
                String createTableSql = TestResourceUtils.toString(SCHEMA_FILE);
                sourceRepository.executeDDL(createTableSql);
                log.info("[{}] 테스트 공원 테이블 생성/업데이트 완료", collectorId);
            }
        } catch (IOException e) {
            log.error("[{}] SQL 파일 읽기 실패: {}", collectorId, e.getMessage(), e);
            throw new RuntimeException("SQL 파일 읽기 실패", e);
        }
    }

    @Override
    public void collect() {
        try {
            Reader dataFileReader = TestResourceUtils.toReader(DATA_FILE);
            int rowsCopied = csvSourceFileLoader.load(dataFileReader, TABLE_NAME);
            log.info("[{}] 총 {}개의 공원 데이터를 가져왔습니다.", collectorId, rowsCopied);
        } catch (IOException | SQLException e) {
            log.error("[{}] CSV 파일 처리 실패: {}", collectorId, e.getMessage(), e);
            throw new RuntimeException("데이터 수집 실패", e);
        }
    }

    @Override
    public void preprocess() {
        try {
            sourceRepository.addLocationColumn(TABLE_NAME);
            log.info("[{}] 공원 테이블에 location 컬럼을 추가했습니다.", collectorId);

            int updatedRows = sourceRepository.updateLocationCoordinates(TABLE_NAME);
            log.info("[{}] {}개의 공원 위치 데이터를 업데이트했습니다.", collectorId, updatedRows);
        } catch (Exception e) {
            log.error("[{}] 전처리 작업 실패: {}", collectorId, e.getMessage(), e);
            throw new RuntimeException("전처리 실패", e);
        }
    }
    
    @Override
    public boolean wasUpdated() {
        return needsUpdate;
    }
    
    @Override
    public String toString() {
        return "TestParkSourceCollector{id='" + collectorId + "', needsUpdate=" + needsUpdate + '}';
    }
}