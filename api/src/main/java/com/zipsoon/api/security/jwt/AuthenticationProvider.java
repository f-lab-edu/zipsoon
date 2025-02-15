package com.zipsoon.api.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationProvider {

    private final JwtProvider jwtProvider;
//    private final OauthProvider oauthProvider;
//    private final UserProvider userProvider;

    public Authentication authenticate(HttpServletRequest request) {
        String token = jwtProvider.extractToken(request);

        if (token != null && jwtProvider.validateToken(token)) {
            return jwtProvider.getAuthentication(token);
        }

//        String oauthToken = oauthProvider.extractToken(request);
//        if (oauthToken != null && oauthProvider.validateToken(oauthToken)) {
//            return oauthProvider.getAuthentication(oauthToken);
//        }

//        String email = request.getHeader("email");
//        if (email != null) {
//            return userProvider.authenticateWithEmail(email);
//        }

        throw new AuthenticationException("Authentication failed") {};
    }
}