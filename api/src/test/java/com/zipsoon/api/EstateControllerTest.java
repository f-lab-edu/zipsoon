package com.zipsoon.api;

import com.zipsoon.api.property.dto.ViewportRequest;
import com.zipsoon.common.config.TestDatabaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@Import(TestDatabaseConfig.class)
@MapperScan({"com.zipsoon.api.property.mapper", "com.zipsoon.common.mapper"})
@AutoConfigureMockMvc(addFilters = false)
class PropertyControllerTest {
    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("Viewport 내의 매물을 조회한다")
    @Sql("/datasets/ShouldFindPropertiesWithinDistance.sql")
    void shouldGetPropertiesInViewport() throws Exception {
        ViewportRequest request = new ViewportRequest(
                126.96,
                37.57,
                126.97,
                37.58,
                15
        );

        mockMvc.perform(get("/api/v1/properties/map")
                        .param("swLng", String.valueOf(request.swLng()))
                        .param("swLat", String.valueOf(request.swLat()))
                        .param("neLng", String.valueOf(request.neLng()))
                        .param("neLat", String.valueOf(request.neLat()))
                        .param("zoom", String.valueOf(request.zoom())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}