package com.zipsoon.batch.config;

import com.zipsoon.batch.estate.job.EstateJobRunner;
import com.zipsoon.batch.score.job.ScoreJobRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {
    private final EstateJobRunner estateJobRunner;
    private final ScoreJobRunner scoreJobRunner;

    public void runEstateCollection() {
        jobLauncher.run(estateJob, params);
    }

    public void runSourceCollection() {
        jobLauncher.run(sourceJob, params);
    }

//    public void runScoreCalculation() {
//        jobLauncher.run(scoreJob, params);
//    }

}
