package com.zipsoon.batch.job.migration.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 데이터베이스 스냅샷 이동 및 테이블 비우기 Tasklet
 * 이 Tasklet은 estate와 estate_score 테이블의 데이터를 스냅샷 테이블로 이동하고
 * 기존 테이블을 비우는 작업을 수행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMigrationTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            log.info("[BATCH:TASKLET-START] 데이터베이스 마이그레이션 작업 시작");
            
            // 1. estate_score 테이블의 데이터를 스냅샷으로 이동
            log.info("[BATCH:TASKLET-STEP] estate_score 데이터를 스냅샷으로 이동");
            jdbcTemplate.execute(
                "INSERT INTO estate_score_snapshot " +
                "SELECT * FROM estate_score"
            );
            
            // 2. estate 테이블의 데이터를 스냅샷으로 이동
            log.info("[BATCH:TASKLET-STEP] estate 데이터를 스냅샷으로 이동");
            jdbcTemplate.execute(
                "INSERT INTO estate_snapshot " +
                "SELECT * FROM estate"
            );
            
            // 3. estate, 그리고 관련된 모든 테이블 truncate
            log.info("[BATCH:TASKLET-STEP] estate 테이블(부모 테이블) 비우기");
            jdbcTemplate.execute("TRUNCATE TABLE estate CASCADE");
            
            log.info("[BATCH:TASKLET-RESULT] 테이블 비우기 완료");
            
            log.info("[BATCH:TASKLET-END] 데이터베이스 마이그레이션 작업 완료");
            return RepeatStatus.FINISHED;
        } catch (Exception e) {
            log.error("[BATCH:TASKLET-ERR] 데이터베이스 마이그레이션 실패: {}", e.getMessage(), e);
            throw e;
        }
    }
}