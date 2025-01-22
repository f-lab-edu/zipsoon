package com.zipsoon.zipsoonbatch;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class ZipsoonbatchApplicationTests {

    @Test
    @DisplayName("애플리케이션 컨텍스트가 정상적으로 로드되어야 한다")
    void contextLoads() {
    }

}