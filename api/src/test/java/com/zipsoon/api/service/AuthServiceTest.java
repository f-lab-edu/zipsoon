package com.zipsoon.api.service;

import com.zipsoon.api.application.auth.AuthService;
import com.zipsoon.api.domain.auth.Role;
import com.zipsoon.api.domain.user.User;
import com.zipsoon.api.infrastructure.exception.custom.JwtAuthenticationException;
import com.zipsoon.api.infrastructure.exception.custom.ServiceException;
import com.zipsoon.api.infrastructure.exception.model.ErrorCode;
import com.zipsoon.api.infrastructure.jwt.JwtProvider;
import com.zipsoon.api.infrastructure.repository.user.UserRepository;
import com.zipsoon.api.interfaces.api.auth.dto.AuthToken;
import com.zipsoon.api.interfaces.api.auth.dto.LoginRequest;
import com.zipsoon.api.interfaces.api.auth.dto.SignupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    private User createMockUser() {
        return User.builder()
            .id(1L)
            .email("test@example.com")
            .name("Test User")
            .role(Role.USER)
            .emailVerified(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class SignupTests {

        @Test
        @DisplayName("유효한 이메일과 이름으로 회원가입 성공")
        void shouldSignupSuccessfully_When_ValidEmailAndName() {
            // given
            SignupRequest request = new SignupRequest("new@example.com", "New User");

            when(userRepository.existsByEmail(request.email())).thenReturn(false);
            when(jwtProvider.createAccessToken(any())).thenReturn("mock-access-token");
            when(jwtProvider.createRefreshToken(any())).thenReturn("mock-refresh-token");

            // when
            AuthToken result = authService.signup(request);

            // then
            assertNotNull(result);
            assertEquals("mock-access-token", result.accessToken());
            assertEquals("mock-refresh-token", result.refreshToken());
            assertNotNull(result.expiresAt());

            verify(userRepository).existsByEmail(request.email());
            verify(userRepository).save(any(User.class));
            verify(jwtProvider).createAccessToken(any());
            verify(jwtProvider).createRefreshToken(any());
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입 실패")
        void shouldFailSignup_When_EmailAlreadyExists() {
            // given
            SignupRequest request = new SignupRequest("existing@example.com", "Existing User");

            when(userRepository.existsByEmail(request.email())).thenReturn(true);

            // when & then
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                authService.signup(request);
            });

            assertEquals(ErrorCode.USER_DUPLICATE, exception.getErrorCode());

            verify(userRepository).existsByEmail(request.email());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTests {

        @Test
        @DisplayName("등록된 이메일로 로그인 성공")
        void shouldLoginSuccessfully_When_ValidEmail() {
            // given
            LoginRequest request = new LoginRequest("test@example.com");
            User user = createMockUser();

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(jwtProvider.createAccessToken(any())).thenReturn("mock-access-token");
            when(jwtProvider.createRefreshToken(any())).thenReturn("mock-refresh-token");

            // when
            AuthToken result = authService.login(request);

            // then
            assertNotNull(result);
            assertEquals("mock-access-token", result.accessToken());
            assertEquals("mock-refresh-token", result.refreshToken());
            assertNotNull(result.expiresAt());

            verify(userRepository).findByEmail(request.email());
            verify(jwtProvider).createAccessToken(any());
            verify(jwtProvider).createRefreshToken(any());
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 실패")
        void shouldThrowUserNotFound_When_LoginWithInvalidEmail() {
            // given
            LoginRequest request = new LoginRequest("nonexistent@example.com");

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

            // when & then
            JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class, () -> {
                authService.login(request);
            });

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

            verify(userRepository).findByEmail(request.email());
            verify(jwtProvider, never()).createAccessToken(any());
        }

        @Test
        @DisplayName("잘못된 자격 증명으로 로그인 실패")
        void shouldThrowInvalidCredentials_When_LoginWithIncorrectCredentials() {
            // given
            LoginRequest request = new LoginRequest("test@example.com");
            User user = createMockUser();

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(jwtProvider.createAccessToken(any())).thenThrow(new JwtAuthenticationException(ErrorCode.INVALID_CREDENTIALS));

            // when & then
            JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class, () -> {
                authService.login(request);
            });

            assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());

            verify(userRepository).findByEmail(request.email());
            verify(jwtProvider).createAccessToken(any());
        }
    }

    @Nested
    @DisplayName("인증 결과 테스트")
    class AuthenticationResultTests {

        @Test
        @DisplayName("로그인 성공시 적절한 토큰 발급")
        void shouldGenerateValidToken_When_LoginSuccessful() {
            // given
            LoginRequest request = new LoginRequest("test@example.com");
            User user = createMockUser();

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(jwtProvider.createAccessToken(any())).thenReturn("mock-access-token");
            when(jwtProvider.createRefreshToken(any())).thenReturn("mock-refresh-token");

            // when
            AuthToken token = authService.login(request);

            // then
            assertNotNull(token);
            assertEquals("mock-access-token", token.accessToken());
            assertEquals("mock-refresh-token", token.refreshToken());
            assertNotNull(token.expiresAt());

            verify(userRepository).findByEmail(request.email());
        }

        @Test
        @DisplayName("인증 실패시 예외 발생")
        void shouldThrowException_When_AuthenticationFails() {
            // given
            LoginRequest request = new LoginRequest("test@example.com");
            User user = createMockUser();
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(jwtProvider.createAccessToken(any())).thenThrow(new JwtAuthenticationException(ErrorCode.INVALID_CREDENTIALS));

            // when & then
            assertThrows(JwtAuthenticationException.class, () -> authService.login(request));
        }
    }
}