package com.zipsoon.batch.config;

import com.zipsoon.batch.estate.job.EstateJobRunner;
import com.zipsoon.batch.score.job.EstateScoreJobRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduleConfig {
    private final EstateJobRunner estateJobRunner;
    private final EstateScoreJobRunner estateScoreJobRunner;

    public void runEstateJob() throws Exception {
        estateJobRunner.run();
    }

    public void runScoreJob() throws Exception {
        estateScoreJobRunner.run();
    }

//    public void runScoreCalculation() {
//        jobLauncher.run(scoreJob, params);
//    }

}
