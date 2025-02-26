package com.zipsoon.batch.config;

import com.zipsoon.batch.estate.job.EstateJobRunner;
import com.zipsoon.batch.normalize.job.NormalizeJobRunner;
import com.zipsoon.batch.score.job.ScoreJobRunner;
import com.zipsoon.batch.source.job.ScoreSourceJobRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduleConfig {
    private final EstateJobRunner estateJobRunner;
    private final ScoreSourceJobRunner sourceJobRunner;
    private final ScoreJobRunner scoreJobRunner;
    private final NormalizeJobRunner normalizeJobRunner;

    // 예시: 매일 새벽 2시에 실행
    // @Scheduled(cron = "0 0 2 * * ?")
    public void runEstateJobScheduled() {
        try {
            log.info("Starting scheduled estate data collection");
            estateJobRunner.run();
            runSourceJobAfterEstate();
        } catch (Exception e) {
            log.error("Failed to run scheduled estate job", e);
        }
    }

    public void runSourceJobAfterEstate() {
        try {
            log.info("Triggering source after estate job");
            sourceJobRunner.run();
            runScoreJobAfterEstate();
        } catch (Exception e) {
            log.error("Failed to run source job", e);
        }
    }

    public void runScoreJobAfterEstate() {
        try {
            log.info("Triggering score calculation after source job");
            scoreJobRunner.run();
            runNormalizeJobAfterScore();
        } catch (Exception e) {
            log.error("Failed to run score job", e);
        }
    }

    public void runNormalizeJobAfterScore() {
        try {
            log.info("Triggering normalization after score job");
            normalizeJobRunner.run();
        } catch (Exception e) {
            log.error("Failed to run normalize job", e);
        }
    }

}
