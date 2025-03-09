package com.zipsoon.batch.infrastructure.processor.source.loader;

import com.zipsoon.batch.application.service.source.loader.SourceFileLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CsvSourceFileLoader implements SourceFileLoader {
    private final DataSource dataSource;

    @Override
    public int load(Reader reader, String tableName) throws IOException, SQLException {
        try (Reader safeReader = reader) {
            // 스프링이 관리하는 트랜잭션 Connection 획득
            Connection conn = DataSourceUtils.getConnection(dataSource);
            try {
                BaseConnection pgConn = conn.unwrap(BaseConnection.class);
                CopyManager copyManager = new CopyManager(pgConn);

                int rowsCopied = (int) copyManager.copyIn(
                    "COPY " + tableName + " FROM STDIN WITH CSV HEADER",
                    safeReader
                );

                log.info("테이블 {}에 {}개 행을 성공적으로 복사했습니다", tableName, rowsCopied);
                return rowsCopied;
            } catch (Exception e) {
                log.error("테이블 {}에 데이터 복사 중 오류 발생: {}", tableName, e.getMessage());
                throw e;
            } finally {
                // 스프링이 관리하는 Connection 반환 (실제로 닫지 않음)
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }
}
