package com.zipsoon.api.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtTokenProvider tokenProvider;
    private static final String BEARER_PREFIX = "Bearer ";

    public String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }

    public Authentication getAuthentication(String token) {
        return tokenProvider.getAuthentication(token);
    }
}

