package com.zipsoon.api.security.filter;

import com.zipsoon.common.exception.ErrorCode;
import com.zipsoon.common.exception.domain.AuthenticationException;
import com.zipsoon.api.security.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
       @NonNull HttpServletRequest request,
       @NonNull HttpServletResponse response,
       @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = jwtProvider.extractToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (!jwtProvider.validateToken(token)) {
                throw new AuthenticationException(ErrorCode.INVALID_CREDENTIALS);
            }

            Authentication auth = jwtProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}

