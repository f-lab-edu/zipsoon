package com.zipsoon.api.security.dto;

import java.time.LocalDateTime;

public record AuthToken(
    String accessToken,
    String refreshToken,
    LocalDateTime expiresAt
) {}