package com.zipsoon.api.infrastructure.repository.user;


import com.zipsoon.api.domain.user.User;
import com.zipsoon.api.interfaces.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final UserMapper userMapper;
    
    public Optional<User> findById(Long id) {
        return userMapper.findById(id);
    }
    
    public Optional<User> findByEmail(String email) {
        return userMapper.findByEmail(email);
    }
    
    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        return userMapper.findByProviderAndProviderId(provider, providerId);
    }
    
    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }
    
    public void save(User user) {
        userMapper.save(user);
    }
    
    public void delete(Long id) {
        userMapper.delete(id);
    }
}
