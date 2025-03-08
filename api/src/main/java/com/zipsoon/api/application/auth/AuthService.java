package com.zipsoon.api.application.auth;

import com.zipsoon.api.domain.auth.AuthenticationResult;
import com.zipsoon.api.domain.auth.Role;
import com.zipsoon.api.domain.auth.UserPrincipal;
import com.zipsoon.api.domain.user.User;
import com.zipsoon.api.infrastructure.exception.custom.JwtAuthenticationException;
import com.zipsoon.api.infrastructure.exception.custom.ServiceException;
import com.zipsoon.api.infrastructure.jwt.JwtProvider;
import com.zipsoon.api.infrastructure.repository.user.UserRepository;
import com.zipsoon.api.interfaces.api.auth.dto.AuthTokenResponse;
import com.zipsoon.api.interfaces.api.auth.dto.LoginRequest;
import com.zipsoon.api.interfaces.api.auth.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    public AuthTokenResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ServiceException(USER_DUPLICATE);
        }

        var newUser = User.of(
                        request.email(),
                        request.name(),
                        Role.USER
                    );
        userRepository.save(newUser);

        var authResult = authenticate(newUser);
        return createAuthToken(authResult);
    }

    @Transactional(readOnly = true)
    public AuthTokenResponse login(LoginRequest request) {
        var user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new JwtAuthenticationException(USER_NOT_FOUND));

        var authResult = authenticate(user);
        return createAuthToken(authResult);
    }

    private AuthTokenResponse createAuthToken(AuthenticationResult authResult) {
        if (!authResult.isValid()) {
            throw new JwtAuthenticationException(INVALID_CREDENTIALS);
        }

        return new AuthTokenResponse(
            jwtProvider.createAccessToken(authResult.authentication()),
            jwtProvider.createRefreshToken(authResult.authentication()),
            authResult.authenticatedAt().plusDays(14)
        );
    }

    private AuthenticationResult authenticate(User user) {
        var principal = UserPrincipal.create(user);
        var authentication = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.getAuthorities()
        );

        return AuthenticationResult.of(user, authentication);
    }

}
