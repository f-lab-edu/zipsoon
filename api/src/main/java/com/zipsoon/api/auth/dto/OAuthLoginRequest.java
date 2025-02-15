package com.zipsoon.api.auth.dto;

import com.zipsoon.api.auth.model.AuthProvider;

public record OAuthLoginRequest(
    AuthProvider provider,
    String accessToken
) {}
