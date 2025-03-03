package com.zipsoon.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipsoon.api.interfaces.api.auth.AuthController;
import com.zipsoon.api.interfaces.api.auth.dto.LoginRequest;
import com.zipsoon.api.application.auth.AuthService;
import com.zipsoon.api.infrastructure.exception.custom.ServiceException;
import com.zipsoon.api.infrastructure.exception.model.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("잘못된 요청값으로 로그인하면 400 Bad Request 반환")
    void shouldReturnBadRequest_When_LoginWithInvalidValue() throws Exception {
        when(authService.login(any())).thenThrow(new ServiceException(ErrorCode.BAD_REQUEST));

        var request = new LoginRequest("test@test.com");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 로그인 시 404 Not Found 반환")
    void shouldReturnNotFound_When_UserNotExists() throws Exception {
        when(authService.login(any())).thenThrow(new ServiceException(ErrorCode.USER_NOT_FOUND));

        var request = new LoginRequest("test@test.com");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("리소스 접근 권한이 없을 때 403 Forbidden 반환")
    void shouldReturnForbidden_When_AccessDenied() throws Exception {
        when(authService.login(any())).thenThrow(new AccessDeniedException("Access denied"));

        var request = new LoginRequest("test@test.com");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
}