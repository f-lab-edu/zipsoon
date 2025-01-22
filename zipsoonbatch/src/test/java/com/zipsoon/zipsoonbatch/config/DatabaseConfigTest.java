package com.zipsoon.zipsoonbatch.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class DatabaseConfigTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("데이터베이스 연결이 정상적으로 설정되어야 한다")
    void databaseIsConnected(@Autowired JdbcTemplate jdbcTemplate) {
        String version = jdbcTemplate.queryForObject(
            "SELECT version()",
            String.class
        );
        log.info("Database version: {}", version);

        assertThat(version)
            .isNotNull()
            .contains("PostgreSQL");
    }

    @Test
    @DisplayName("PostGIS 확장이 설치되어 있어야 한다")
    void postGisExtensionExists(@Autowired JdbcTemplate jdbcTemplate) {
        String result = jdbcTemplate.queryForObject(
            "SELECT extversion FROM pg_extension WHERE extname = 'postgis'",
            String.class
        );
        log.info("PostGIS version: {}", result);

        assertThat(result).isNotNull();
    }

}
