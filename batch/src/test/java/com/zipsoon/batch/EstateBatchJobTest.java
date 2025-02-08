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
public class PropertyBatchJobTest {

    @Autowired
    private EstateSnapshotRepository psr;

//    @Test
//    void testPropertyBatchJob_InsertPropertySnapshot() throws Exception {
//        propertyJobRunner.run();
//
//        List<PropertySnapshot> insertedSnapshots = psr.findAll();
//
//        assertThat(insertedSnapshots).hasSize(20);
//
//        PropertySnapshot firstSnapshot = insertedSnapshots.get(0);
//        assertThat(firstSnapshot.getPlatformType()).isEqualTo(PropertySnapshot.PlatformType.네이버);
//    }
//

}