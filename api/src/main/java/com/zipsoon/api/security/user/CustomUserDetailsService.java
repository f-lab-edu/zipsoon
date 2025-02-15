package com.zipsoon.api.security.user;

import com.zipsoon.api.auth.model.UserPrincipal;
import com.zipsoon.api.exception.custom.ServiceException;
import com.zipsoon.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.zipsoon.api.exception.model.ErrorCode.USER_NOT_FOUND;

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
