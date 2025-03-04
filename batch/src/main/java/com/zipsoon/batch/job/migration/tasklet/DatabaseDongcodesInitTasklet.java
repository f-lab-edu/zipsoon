package com.zipsoon.batch.job.migration.tasklet;

import com.zipsoon.batch.infrastructure.processor.source.loader.CsvSourceFileLoader;
import com.zipsoon.batch.infrastructure.processor.source.loader.ResourceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

/**
 * 법정동코드 테이블 초기화 및 데이터 로드 Tasklet
 * 이 Tasklet은 dongcodes 테이블을 생성하고 CSV 파일에서 데이터를 로드합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseDongcodesInitTasklet implements Tasklet {

    private static final String TABLE_NAME = "dongcodes";
    private static final String SCHEMA_FILE = "source/sql/dongcode-resource-query.sql";
    private static final String DATA_FILE = "source/data/dongcode-resource-data.csv";

    private final JdbcTemplate jdbcTemplate;
    private final CsvSourceFileLoader csvSourceFileLoader;

    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            log.info("Starting dongcodes table initialization tasklet");
            
            // 1. 테이블 존재 여부 확인 및 드롭
            checkAndDropTable();
            
            // 2. 테이블 생성
            createTable();
            
            // 3. 데이터 로드
            loadData();
            
            log.info("Dongcodes table initialization completed successfully");
            return RepeatStatus.FINISHED;
        } catch (Exception e) {
            log.error("Failed to initialize dongcodes table: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void checkAndDropTable() {
        try {
            log.info("Checking if dongcodes table exists");
            // 테이블 존재 여부 확인
            Boolean tableExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = ?)",
                Boolean.class,
                TABLE_NAME
            );
            
            if (Boolean.TRUE.equals(tableExists)) {
                log.info("Dropping existing dongcodes table");
                jdbcTemplate.execute("DROP TABLE IF EXISTS " + TABLE_NAME);
                log.info("Successfully dropped dongcodes table");
            } else {
                log.info("Table dongcodes does not exist, no need to drop");
            }
        } catch (Exception e) {
            log.warn("Error checking or dropping dongcodes table: {}", e.getMessage());
            // 계속 진행 (테이블이 없더라도 다음 단계로)
        }
    }
    
    private void createTable() throws IOException {
        try {
            log.info("Creating dongcodes table");
            String createTableSql = ResourceUtils.toString(SCHEMA_FILE);
            jdbcTemplate.execute(createTableSql);
            log.info("Successfully created dongcodes table");
        } catch (Exception e) {
            log.error("Failed to create dongcodes table: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create dongcodes table", e);
        }
    }
    
    private void loadData() throws IOException, SQLException {
        try {
            log.info("Loading data into dongcodes table");
            Reader dataFileReader = ResourceUtils.toReader(DATA_FILE);
            int rowsCopied = csvSourceFileLoader.load(dataFileReader, TABLE_NAME);
            log.info("Successfully loaded {} rows into dongcodes table", rowsCopied);
        } catch (Exception e) {
            log.error("Failed to load data into dongcodes table: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load data into dongcodes table", e);
        }
    }
}