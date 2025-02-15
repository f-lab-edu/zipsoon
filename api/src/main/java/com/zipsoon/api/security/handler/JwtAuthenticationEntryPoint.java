package com.zipsoon.api.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipsoon.api.exception.custom.JwtAuthenticationException;
import com.zipsoon.api.exception.model.ErrorCode;
import com.zipsoon.api.exception.model.ErrorResponse;
import com.zipsoon.api.exception.model.ErrorResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        if (authException instanceof JwtAuthenticationException) {
            errorCode = ((JwtAuthenticationException) authException).getErrorCode();
        }

        ErrorResponse errorResponse = ErrorResponseFactory.from(errorCode);
        response.setStatus(errorCode.getHttpStatusCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}