package com.zipsoon.api.auth.domain;

import com.zipsoon.api.user.domain.User;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;

public record AuthenticationResult(
    User user,
    Authentication authentication,
    LocalDateTime authenticatedAt
) {
    public static AuthenticationResult of(User user, Authentication authentication) {
        return new AuthenticationResult(user, authentication, LocalDateTime.now());
    }

    public boolean isValid() {
        return user != null && authentication != null;
    }
}

