package com.zipsoon.api.auth.service;

import com.zipsoon.api.auth.dto.LoginRequest;
import com.zipsoon.api.auth.dto.SignupRequest;
import com.zipsoon.common.domain.user.Role;
import com.zipsoon.common.domain.user.User;
import com.zipsoon.common.domain.user.UserRepository;
import com.zipsoon.common.exception.ErrorCode;
import com.zipsoon.common.exception.domain.AuthenticationException;
import com.zipsoon.common.exception.domain.InvalidValueException;
import com.zipsoon.common.security.dto.AuthToken;
import com.zipsoon.common.security.jwt.JwtTokenProvider;
import com.zipsoon.common.security.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthToken signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new InvalidValueException(ErrorCode.USER_DUPLICATE);
        }

        User user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .name(request.name())
            .role(Role.USER)
            .build();

        userRepository.save(user);
        return createAuthToken(user);
    }

    @Transactional(readOnly = true)
    public AuthToken login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new AuthenticationException(ErrorCode.USER_NOT_FOUND));

        if (!user.isValidPassword(passwordEncoder, request.password())) {
            throw new AuthenticationException(ErrorCode.USER_INVALID_OAUTH_PROVIDER);
        }

        return createAuthToken(user);
    }

    private AuthToken createAuthToken(User user) {
        UserPrincipal principal = UserPrincipal.create(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.getAuthorities()
        );

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        return new AuthToken(
            accessToken,
            refreshToken,
            LocalDateTime.now().plusDays(14)
        );
    }

}
