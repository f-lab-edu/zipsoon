package com.zipsoon.api.user.service;

import com.zipsoon.api.user.dto.*;
import com.zipsoon.common.domain.user.User;
import com.zipsoon.common.exception.ErrorCode;
import com.zipsoon.common.exception.domain.AuthenticationException;
import com.zipsoon.common.exception.domain.InvalidValueException;
import com.zipsoon.common.exception.domain.ResourceNotFoundException;
import com.zipsoon.common.repository.UserRepository;
import com.zipsoon.common.security.dto.AuthToken;
import com.zipsoon.common.security.jwt.JwtTokenProvider;
import com.zipsoon.common.security.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthToken signup(UserSignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new InvalidValueException(ErrorCode.USER_DUPLICATE);
        }

        User user = User.createNewUser(request.email(), request.password(), request.name(), passwordEncoder);
        userRepository.save(user);
        return createAuthToken(user);
    }

    @Transactional(readOnly = true)
    public AuthToken login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthenticationException(ErrorCode.USER_NOT_FOUND));

        if (!user.isValidPassword(passwordEncoder, request.password())) {
            throw new AuthenticationException(ErrorCode.INVALID_PASSWORD);
        }

        return createAuthToken(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        return UserProfileResponse.from(user);
    }

    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        user.updateProfile(request.name(), request.imageUrl());
        return UserProfileResponse.from(user);
    }

    public void deleteAccount(Long userId) {
        userRepository.delete(userId);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        if (!user.isValidPassword(passwordEncoder, request.currentPassword())) {
            throw new InvalidValueException(ErrorCode.INVALID_PASSWORD);
        }

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    private AuthToken createAuthToken(User user) {
        UserPrincipal principal = UserPrincipal.create(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );

        return new AuthToken(
                jwtTokenProvider.createAccessToken(authentication),
                jwtTokenProvider.createRefreshToken(authentication),
                LocalDateTime.now().plusDays(14)
        );
    }

}
