package com.zipsoon.batch.source.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkSourceCollector implements ScoreSourceCollector {
    private final DataSource dataSource;
    
    private static final String PARK_SCHEMA_PATH = "source/sql/park-score-resource-query.sql";
    private static final String PARK_DATA_PATH = "source/data/park-score-resource-data.csv";

    @Override
    public void create() {
        try {
            String sql = loadResourceAsString(PARK_SCHEMA_PATH);
            executeStatement(sql);
            log.info("공원 테이블 생성/업데이트 완료");
        } catch (IOException e) {
            log.error("SQL 파일 읽기 실패: {}", e.getMessage(), e);
            throw new RuntimeException("SQL 파일 읽기 실패", e);
        } catch (SQLException e) {
            log.error("데이터베이스 작업 실패: {}", e.getMessage(), e);
            throw new RuntimeException("데이터베이스 작업 실패", e);
        }
    }

    @Override
    public void collect() {
        try {
            long rowsCopied = loadCsvData();
            updateLocationPoints();
            log.info("총 {}개의 공원 데이터를 가져왔습니다.", rowsCopied);
        } catch (IOException e) {
            log.error("CSV 파일 읽기 실패: {}", e.getMessage(), e);
            throw new RuntimeException("CSV 파일 읽기 실패", e);
        } catch (SQLException e) {
            log.error("데이터베이스 작업 실패: {}", e.getMessage(), e);
            throw new RuntimeException("데이터베이스 작업 실패", e);
        }
    }
    
    @Override
    public boolean validate() {
        return true;
    }
    
    private String loadResourceAsString(String resourcePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        return new String(FileCopyUtils.copyToByteArray(resource.getInputStream()));
    }
    
    private void executeStatement(String sql) throws SQLException {
        try (
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement()
        ) {
            conn.setAutoCommit(true);
            stmt.execute(sql);
        }
    }
    
    private long loadCsvData() throws IOException, SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                ClassPathResource resource = new ClassPathResource(PARK_DATA_PATH);
                
                try (
                    InputStream inputStream = resource.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                ) {
                    
                    BaseConnection pgConn = conn.unwrap(BaseConnection.class);
                    CopyManager copyManager = new CopyManager(pgConn);
                    
                    long rowsCopied = copyManager.copyIn("COPY parks FROM STDIN WITH CSV HEADER", reader);
                    conn.commit();
                    return rowsCopied;
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
    
    private void updateLocationPoints() throws SQLException {
        String alterSql = "ALTER TABLE parks ADD COLUMN IF NOT EXISTS location geometry(Point, 4326)";
        String updateSql = "UPDATE parks " +
                           "SET location = ST_SetSRID(ST_Point(경도, 위도), 4326) " +
                           "WHERE 위도 IS NOT NULL AND 경도 IS NOT NULL";
        
        try (
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement()
        ) {
            conn.setAutoCommit(true);
            
            stmt.execute(alterSql);
            log.info("공원 테이블에 location 컬럼을 추가했습니다.");
            
            int updatedRows = stmt.executeUpdate(updateSql);
            log.info("{}개의 공원 위치 데이터를 업데이트했습니다.", updatedRows);
        }
    }
}