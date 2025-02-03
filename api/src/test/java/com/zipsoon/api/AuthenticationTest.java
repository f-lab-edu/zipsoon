package com.zipsoon.api;

import com.zipsoon.api.auth.dto.LoginRequest;
import com.zipsoon.api.auth.dto.SignupRequest;
import com.zipsoon.api.auth.service.UserService;
import com.zipsoon.api.security.filter.JwtAuthenticationFilter;
import com.zipsoon.common.domain.user.UserRepository;
import com.zipsoon.common.exception.ErrorCode;
import com.zipsoon.common.exception.domain.AuthenticationException;
import com.zipsoon.common.exception.domain.InvalidValueException;
import com.zipsoon.common.security.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class AuthenticationTest {
    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시도 시 USER_NOT_FOUND 예외 발생")
    void exceptionTest_UserNotFound() {
        LoginRequest loginRequest = new LoginRequest("nonexistent@email.com", "password");

        assertThrows(AuthenticationException.class, () -> {
            userService.login(loginRequest);
        }, ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시도 시 USER_INVALID_OAUTH_PROVIDER 예외 발생")
    void exceptionTest_InvalidPassword() {
        LoginRequest loginRequest = new LoginRequest("user@email.com", "wrongpassword");

        assertThrows(AuthenticationException.class, () -> {
            userService.login(loginRequest);
        }, ErrorCode.USER_INVALID_OAUTH_PROVIDER.getMessage());
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입 시도 시 USER_DUPLICATE 예외 발생")
    void exceptionTest_DuplicateEmail() {
        SignupRequest signupRequest = new SignupRequest(
                "existing@email.com", "password", "name");

        assertThrows(InvalidValueException.class, () -> {
            userService.signup(signupRequest);
        }, ErrorCode.USER_DUPLICATE.getMessage());
    }

    @Test
    @DisplayName("유효하지 않은 JWT 토큰으로 접근 시도 시 AUTH_UNAUTHORIZED 예외 발생")
    void exceptionTest_InvalidJwtToken() {
        // given
        String invalidToken = "invalid.jwt.token";

        // when & then
        when(jwtProvider.validateToken(invalidToken)).thenReturn(false);
        assertThrows(AuthenticationException.class, () -> {
            if (!jwtProvider.validateToken(invalidToken)) {
                throw new AuthenticationException(ErrorCode.AUTH_UNAUTHORIZED);
            }
        }, ErrorCode.AUTH_UNAUTHORIZED.getMessage());

        verify(jwtProvider).validateToken(invalidToken);
    }
}