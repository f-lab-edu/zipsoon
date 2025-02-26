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

    @Override
    public void create() {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);

            ClassPathResource resource = new ClassPathResource("source/sql/park-score-resource-query.sql");
            String sql = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()));

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                log.info("공원 테이블 생성/업데이트 완료");
            }
        } catch (SQLException e) {
            log.error("데이터베이스 작업 실패: {}", e.getMessage(), e);
            throw new RuntimeException("데이터베이스 작업 실패", e);
        } catch (IOException e) {
            log.error("SQL 파일 읽기 실패: {}", e.getMessage(), e);
            throw new RuntimeException("SQL 파일 읽기 실패", e);
        }
    }

    @Override
    public void collect() {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ClassPathResource resource = new ClassPathResource("source/data/park-score-resource-data.csv");
                try (InputStream inputStream = resource.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    
                    BaseConnection pgConn = conn.unwrap(BaseConnection.class);
                    CopyManager copyManager = new CopyManager(pgConn);
                    
                    long rowsCopied = copyManager.copyIn("COPY parks FROM STDIN WITH CSV HEADER", reader);
                    conn.commit();
                    
                    log.info("총 {}개의 공원 데이터를 가져왔습니다.", rowsCopied);
                }
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    log.error("롤백 실패", ex);
                }
                throw e;
            }
        } catch (SQLException e) {
            log.error("데이터베이스 작업 실패: {}", e.getMessage(), e);
            throw new RuntimeException("데이터베이스 작업 실패", e);
        } catch (IOException e) {
            log.error("CSV 파일 읽기 실패: {}", e.getMessage(), e);
            throw new RuntimeException("CSV 파일 읽기 실패", e);
        }
    }
    
    @Override
    public boolean validate() {
        return true;
    }
}