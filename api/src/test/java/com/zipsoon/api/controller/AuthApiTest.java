package com.zipsoon.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipsoon.api.application.auth.AuthService;
import com.zipsoon.api.domain.auth.Role;
import com.zipsoon.api.domain.user.User;
import com.zipsoon.api.infrastructure.exception.custom.ServiceException;
import com.zipsoon.api.infrastructure.exception.model.ErrorCode;
import com.zipsoon.api.interfaces.api.auth.AuthController;
import com.zipsoon.api.interfaces.api.auth.dto.AuthToken;
import com.zipsoon.api.interfaces.api.auth.dto.LoginRequest;
import com.zipsoon.api.interfaces.api.auth.dto.SignupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    // 테스트용 토큰 응답 생성
    private AuthToken createMockToken() {
        return new AuthToken(
            "mock-access-token",
            "mock-refresh-token",
            LocalDateTime.now().plusHours(1)
        );
    }

    // 테스트용 사용자 생성
    private User createMockUser() {
        return User.builder()
            .id(1L)
            .email("test@example.com")
            .name("Test User")
            .role(Role.USER)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class SignupTests {

        @Test
        @DisplayName("유효한 이메일과 이름으로 회원가입 성공")
        void shouldSignupSuccessfully_When_ValidEmailAndName() throws Exception {
            // given
            SignupRequest request = new SignupRequest("new@example.com", "New User");
            AuthToken mockToken = createMockToken();

            when(authService.signup(any(SignupRequest.class))).thenReturn(mockToken);

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is(mockToken.accessToken())))
                .andExpect(jsonPath("$.refreshToken", is(mockToken.refreshToken())))
                .andExpect(jsonPath("$.expiresAt", notNullValue()));

            verify(authService).signup(any(SignupRequest.class));
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입 실패")
        void shouldFailSignup_When_EmailAlreadyExists() throws Exception {
            // given
            SignupRequest request = new SignupRequest("existing@example.com", "Existing User");

            when(authService.signup(any(SignupRequest.class)))
                .thenThrow(new ServiceException(ErrorCode.USER_DUPLICATE));

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is(ErrorCode.USER_DUPLICATE.getHttpStatusCode())));

            verify(authService).signup(any(SignupRequest.class));
        }

        @Test
        @DisplayName("이메일 형식이 잘못된 경우 회원가입 실패")
        void shouldFailSignup_When_InvalidEmailFormat() throws Exception {
            // given
            SignupRequest request = new SignupRequest("invalid-email", "Invalid User");

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            verify(authService, never()).signup(any(SignupRequest.class));
        }

        @Test
        @DisplayName("필수 필드 누락시 회원가입 실패 (이메일 누락)")
        void shouldFailSignup_When_EmailMissing() throws Exception {
            // given
            SignupRequest request = new SignupRequest("", "Missing Email User");

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            verify(authService, never()).signup(any(SignupRequest.class));
        }

        @Test
        @DisplayName("필수 필드 누락시 회원가입 실패 (이름 누락)")
        void shouldFailSignup_When_NameMissing() throws Exception {
            // given
            SignupRequest request = new SignupRequest("missing@example.com", "");

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            verify(authService, never()).signup(any(SignupRequest.class));
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTests {

        @Test
        @DisplayName("등록된 이메일로 로그인 성공")
        void shouldLoginSuccessfully_When_ValidEmail() throws Exception {
            // given
            LoginRequest request = new LoginRequest("existing@example.com");
            AuthToken mockToken = createMockToken();

            when(authService.login(any(LoginRequest.class))).thenReturn(mockToken);

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is(mockToken.accessToken())))
                .andExpect(jsonPath("$.refreshToken", is(mockToken.refreshToken())))
                .andExpect(jsonPath("$.expiresAt", notNullValue()));

            verify(authService).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 실패")
        void shouldFailLogin_When_EmailNotFound() throws Exception {
            // given
            LoginRequest request = new LoginRequest("nonexistent@example.com");

            when(authService.login(any(LoginRequest.class)))
                .thenThrow(new ServiceException(ErrorCode.USER_NOT_FOUND));

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(ErrorCode.USER_NOT_FOUND.getHttpStatusCode())));

            verify(authService).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("잘못된 자격 증명으로 로그인 실패")
        void shouldFailLogin_When_InvalidCredentials() throws Exception {
            // given
            LoginRequest request = new LoginRequest("invalid@example.com");

            when(authService.login(any(LoginRequest.class)))
                .thenThrow(new ServiceException(ErrorCode.INVALID_CREDENTIALS));

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is(ErrorCode.INVALID_CREDENTIALS.getHttpStatusCode())));

            verify(authService).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("이메일 형식이 잘못된 경우 로그인 실패")
        void shouldFailLogin_When_InvalidEmailFormat() throws Exception {
            // given
            LoginRequest request = new LoginRequest("invalid-email");

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            verify(authService, never()).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("필수 필드 누락시 로그인 실패 (이메일 누락)")
        void shouldFailLogin_When_EmailMissing() throws Exception {
            // given
            LoginRequest request = new LoginRequest("");

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            verify(authService, never()).login(any(LoginRequest.class));
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class TokenValidationTests {

        @Test
        @DisplayName("리소스 접근 권한이 없을 때 403 Forbidden 반환")
        void shouldReturnForbidden_When_AccessDenied() throws Exception {
            // given
            LoginRequest request = new LoginRequest("test@example.com");

            when(authService.login(any())).thenThrow(new AccessDeniedException("Access denied"));

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("만료된 토큰 사용 시 401 Unauthorized 반환")
        void shouldReturnUnauthorized_When_TokenExpired() throws Exception {
            // given
            LoginRequest request = new LoginRequest("test@example.com");

            when(authService.login(any())).thenThrow(new ServiceException(ErrorCode.EXPIRED_TOKEN));

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is(ErrorCode.EXPIRED_TOKEN.getHttpStatusCode())));
        }

        @Test
        @DisplayName("잘못된 토큰 형식 사용 시 401 Unauthorized 반환")
        void shouldReturnUnauthorized_When_MalformedToken() throws Exception {
            // given
            LoginRequest request = new LoginRequest("test@example.com");

            when(authService.login(any())).thenThrow(new ServiceException(ErrorCode.MALFORMED_TOKEN));

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is(ErrorCode.MALFORMED_TOKEN.getHttpStatusCode())));
        }
    }
}