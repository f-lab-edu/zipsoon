package com.zipsoon.api;

import com.zipsoon.api.auth.dto.UserLoginRequest;
import com.zipsoon.api.auth.dto.UserSignupRequest;
import com.zipsoon.api.auth.service.UserService;
import com.zipsoon.common.domain.user.Role;
import com.zipsoon.common.domain.user.User;
import com.zipsoon.common.domain.user.UserRepository;
import com.zipsoon.common.exception.domain.AuthenticationException;
import com.zipsoon.common.exception.domain.InvalidValueException;
import com.zipsoon.common.security.dto.AuthToken;
import com.zipsoon.common.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserExceptionTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시도 시 USER_NOT_FOUND 예외 발생")
    void exceptionTest_UserNotFound() {
       var request = new UserLoginRequest("test@test.com", "password");
       when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

       assertThrows(AuthenticationException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시도 시 USER_INVALID_OAUTH_PROVIDER 예외 발생")
    void exceptionTest_InvalidPassword() {
       var request = new UserLoginRequest("test@test.com", "wrong");
       User user = User.builder()
               .email(request.email())
               .password("correct")
               .role(Role.USER)
               .build();
       when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
       when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

       assertThrows(AuthenticationException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입 시도 시 USER_DUPLICATE 예외 발생")
    void exceptionTest_DuplicateEmail() {
       var request = new UserSignupRequest("test@test.com", "password", "name");
       when(userRepository.existsByEmail(request.email())).thenReturn(true);

       assertThrows(InvalidValueException.class, () -> userService.signup(request));
    }

    @Test
    @DisplayName("유효하지 않은 JWT 토큰으로 접근 시도 시 AUTH_UNAUTHORIZED 예외 발생")
    void exceptionTest_InvalidJwtToken() {
        // given
       var request = new UserSignupRequest("test@test.com", "password", "name");
       when(userRepository.existsByEmail(request.email())).thenReturn(false);
       when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
       when(jwtTokenProvider.createAccessToken(any())).thenReturn("accessToken");
       when(jwtTokenProvider.createRefreshToken(any())).thenReturn("refreshToken");

        // when & then
       AuthToken token = userService.signup(request);

       assertThat(token.accessToken()).isEqualTo("accessToken");
       assertThat(token.refreshToken()).isEqualTo("refreshToken");
       verify(userRepository).save(any(User.class));
    }
}