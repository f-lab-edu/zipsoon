package com.zipsoon.api.interfaces.api.auth.dto;

import java.time.LocalDateTime;

public record AuthToken(
    String accessToken,
    String refreshToken,
    LocalDateTime expiresAt
) {}