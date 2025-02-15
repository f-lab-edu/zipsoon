package com.zipsoon.api.user.service;

import com.zipsoon.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void deleteAccount(Long userId) {
        userRepository.delete(userId);
    }

}
