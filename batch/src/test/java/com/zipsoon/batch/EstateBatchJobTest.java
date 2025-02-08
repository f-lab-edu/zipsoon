package com.zipsoon.batch;

import com.zipsoon.common.config.TestDatabaseConfig;
import com.zipsoon.common.repository.EstateSnapshotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Slf4j
@SpringBootTest
@SpringBatchTest
@Import(TestDatabaseConfig.class)
public class EstateBatchJobTest {

    @Autowired
    private EstateSnapshotRepository esr;

}