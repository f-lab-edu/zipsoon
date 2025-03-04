package com.zipsoon.api.infrastructure.jwt;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationProvider {

    private final JwtProvider jwtProvider;

    public Authentication authenticate(HttpServletRequest request) {
        String token = jwtProvider.extractToken(request);

        if (token != null && jwtProvider.validateToken(token)) {
            return jwtProvider.getAuthentication(token);
        }

        throw new AuthenticationException("Authentication failed") {};
    }
}