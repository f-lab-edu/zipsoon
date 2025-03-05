package com.zipsoon.api.controller;

import com.zipsoon.api.application.estate.EstateService;
import com.zipsoon.api.application.estate.ScoreService;
import com.zipsoon.api.infrastructure.exception.custom.ServiceException;
import com.zipsoon.api.infrastructure.exception.model.ErrorCode;
import com.zipsoon.api.interfaces.api.estate.EstateController;
import com.zipsoon.api.interfaces.api.estate.dto.ViewportRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EstateController.class)
@AutoConfigureMockMvc(addFilters = false)
class EstateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstateService estateService;

    @MockitoBean
    private ScoreService scoreService;

    @Test
    @DisplayName("매물이 없는 좌표 조회 시 404 Not Found 반환")
    void shouldReturnNotFound_When_NoEstatesInViewport() throws Exception {
        ViewportRequest request = new ViewportRequest(180.0, 90.0, 180.0, 90.0, 22);

        when(estateService.findEstatesInViewport(any(), any()))
            .thenThrow(new ServiceException(ErrorCode.ESTATE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/estates/map")
                .param("swLng", String.valueOf(request.swLng()))
                .param("swLat", String.valueOf(request.swLat()))
                .param("neLng", String.valueOf(request.neLng()))
                .param("neLat", String.valueOf(request.neLat()))
                .param("zoom", String.valueOf(request.zoom())))
            .andExpect(status().isNotFound());
    }
}