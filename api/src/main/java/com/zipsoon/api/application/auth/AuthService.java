package com.zipsoon.api.application.auth;

import com.zipsoon.api.domain.auth.AuthenticationResult;
import com.zipsoon.api.interfaces.api.auth.dto.AuthToken;
import com.zipsoon.api.interfaces.api.auth.dto.LoginRequest;
import com.zipsoon.api.interfaces.api.auth.dto.SignupRequest;
import com.zipsoon.api.domain.auth.UserPrincipal;
import com.zipsoon.api.infrastructure.exception.custom.ServiceException;
import com.zipsoon.api.infrastructure.jwt.JwtProvider;
import com.zipsoon.api.domain.auth.Role;
import com.zipsoon.api.domain.user.User;
import com.zipsoon.api.infrastructure.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.zipsoon.api.infrastructure.exception.model.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public AuthToken signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ServiceException(USER_DUPLICATE);
        }

        User newUser = User.builder()
                        .email(request.email())
                        .name(request.name())
                        .role(Role.USER)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
        userRepository.save(newUser);

        AuthenticationResult authResult = authenticate(newUser);
        return createAuthToken(authResult);
    }

    @Transactional(readOnly = true)
    public AuthToken login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

        AuthenticationResult authResult = authenticate(user);
        return createAuthToken(authResult);
    }

    private AuthToken createAuthToken(AuthenticationResult authResult) {
        if (!authResult.isValid()) {
            throw new ServiceException(INVALID_CREDENTIALS);
        }

        return new AuthToken(
            jwtProvider.createAccessToken(authResult.authentication()),
            jwtProvider.createRefreshToken(authResult.authentication()),
            authResult.authenticatedAt().plusDays(14)
        );
    }

    private AuthenticationResult authenticate(User user) {
        UserPrincipal principal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.getAuthorities()
        );

        return AuthenticationResult.of(user, authentication);
    }

}
