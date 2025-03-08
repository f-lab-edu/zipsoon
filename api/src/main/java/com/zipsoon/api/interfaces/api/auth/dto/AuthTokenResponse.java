package com.zipsoon.api.interfaces.api.auth.dto;

import java.time.LocalDateTime;

public record AuthTokenResponse(
    String accessToken,
    String refreshToken,
    LocalDateTime expiresAt
) {}