package com.zipsoon.api.auth.dto;

import java.time.LocalDateTime;

public record AuthToken(
    String accessToken,
    String refreshToken,
    LocalDateTime expiresAt
) {}