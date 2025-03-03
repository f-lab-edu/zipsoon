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
            // 현재 활성화된 트랜잭션 내의 Connection 획득 (Spring 관리)
            Connection conn = DataSourceUtils.getConnection(dataSource);
            try {
                BaseConnection pgConn = conn.unwrap(BaseConnection.class);
                CopyManager copyManager = new CopyManager(pgConn);

                int rowsCopied = (int) copyManager.copyIn(
                    "COPY " + tableName + " FROM STDIN WITH CSV HEADER",
                    safeReader
                );

                log.info("Successfully copied {} rows to table {}", rowsCopied, tableName);
                return rowsCopied;
            } catch (Exception e) {
                log.error("Error copying data to table {}: {}", tableName, e.getMessage());
                throw e;
            } finally {
                // Spring이 관리하는 Connection을 해제 (닫지 않음)
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }
}
