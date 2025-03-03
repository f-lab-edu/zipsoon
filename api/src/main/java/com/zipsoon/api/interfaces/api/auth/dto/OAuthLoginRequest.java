package com.zipsoon.api.interfaces.api.auth.dto;

import com.zipsoon.api.application.auth.AuthProvider;

public record OAuthLoginRequest(
    AuthProvider provider,
    String accessToken
) {}
