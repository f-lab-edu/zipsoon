package com.zipsoon.api.security.jwt;

import com.zipsoon.api.auth.model.UserPrincipal;
import com.zipsoon.api.security.user.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService customUserDetailsService;
    private static final String BEARER_PREFIX = "Bearer ";

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature");
            throw new BadCredentialsException("Invalid JWT signature");
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token");
            throw new AuthenticationException("Expired JWT token") {};
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token");
            throw new AuthenticationException("Unsupported JWT token") {};
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty");
            throw new BadCredentialsException("JWT claims string is empty");
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long userId = Long.parseLong(claims.getSubject());
        UserPrincipal userPrincipal = customUserDetailsService.loadUserById(userId);
        return new UsernamePasswordAuthenticationToken(userPrincipal, "", userPrincipal.getAuthorities());
    }

    public String createAccessToken(Authentication authentication) {
        return createToken(authentication, jwtProperties.getAccessTokenValidityInMilliseconds());
    }

    public String createRefreshToken(Authentication authentication) {
        return createToken(authentication, jwtProperties.getRefreshTokenValidityInMilliseconds());
    }

    private String createToken(Authentication authentication, long validityInMilliseconds) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
            .setSubject(userPrincipal.getName())
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }
}