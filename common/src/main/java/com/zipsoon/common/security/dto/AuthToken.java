package com.zipsoon.common.security.dto;

import java.time.LocalDateTime;

public record AuthToken(
    String accessToken,
    String refreshToken,
    LocalDateTime expiresAt
) {}