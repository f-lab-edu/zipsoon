package com.zipsoon.api.user.service;

import com.zipsoon.api.exception.custom.ServiceException;
import com.zipsoon.api.exception.model.ErrorCode;
import com.zipsoon.api.user.domain.User;
import com.zipsoon.api.user.dto.UpdateProfileRequest;
import com.zipsoon.api.user.dto.UserProfileResponse;
import com.zipsoon.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
        return UserProfileResponse.from(user);
    }

    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
        user.updateProfile(request.name(), request.imageUrl());
        return UserProfileResponse.from(user);
    }

    public void deleteAccount(Long userId) {
        userRepository.delete(userId);
    }

}
