package com.zipsoon.api.application.auth;

import com.zipsoon.api.domain.auth.UserPrincipal;
import com.zipsoon.api.infrastructure.exception.custom.ServiceException;
import com.zipsoon.api.infrastructure.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.zipsoon.api.infrastructure.exception.model.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserPrincipal loadUserById(Long userId) {
        return userRepository.findById(userId)
            .map(UserPrincipal::create)
            .orElseThrow(() -> new ServiceException(USER_NOT_FOUND));
    }
}
