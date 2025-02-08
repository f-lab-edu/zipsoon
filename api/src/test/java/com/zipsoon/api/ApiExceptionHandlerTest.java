package com.zipsoon.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipsoon.api.user.controller.UserController;
import com.zipsoon.api.user.dto.UserLoginRequest;
import com.zipsoon.api.user.service.UserService;
import com.zipsoon.common.exception.ErrorCode;
import com.zipsoon.common.exception.domain.InvalidValueException;
import com.zipsoon.common.exception.domain.ResourceNotFoundException;
import com.zipsoon.common.security.jwt.JwtProvider;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApiExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("validation 어노테이션의 제약조건 위반 시 400 Bad Request")
    void exceptionTest_MethodArgumentNotValid() throws Exception {
        UserLoginRequest invalidRequest = new UserLoginRequest("invalid-email", "");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getHttpStatus().value()));
    }

    @Test
    @DisplayName("잘못된 요청값으로 비즈니스 로직 처리 실패 시 400 Bad Request")
    void exceptionTest_InvalidValue() throws Exception {
        when(userService.login(any()))
            .thenThrow(new InvalidValueException(ErrorCode.BAD_REQUEST));

        UserLoginRequest request = new UserLoginRequest("test@test.com", "password");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getHttpStatus().value()));
    }

    @Test
    @DisplayName("요청한 리소스가 존재하지 않을 시 404 Not Found")
    void exceptionTest_ResourceNotFound() throws Exception {
        when(userService.login(any()))
            .thenThrow(new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        UserLoginRequest request = new UserLoginRequest("test@test.com", "password");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getHttpStatusCode()));
    }

    @Test
    @DisplayName("예상치 못한 서버 오류 발생 시 500 Internal Server Error")
    void exceptionTest_Server() throws Exception {
        when(userService.login(any()))
            .thenThrow(new RuntimeException("Unexpected error"));

        UserLoginRequest request = new UserLoginRequest("test@test.com", "password");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value(ErrorCode.SERVER_ERROR.getHttpStatus().value()));
    }

    @Test
    @DisplayName("리소스 접근 권한이 없을 시 403 Forbidden")
    void exceptionTest_AccessDenied() throws Exception {
        when(userService.login(any()))
            .thenThrow(new AccessDeniedException("Access denied"));

        UserLoginRequest request = new UserLoginRequest("test@test.com", "password");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.getHttpStatus().value()));
    }
}