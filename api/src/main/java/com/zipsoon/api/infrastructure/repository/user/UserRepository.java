package com.zipsoon.api.infrastructure.repository.user;


import com.zipsoon.api.domain.user.User;
import com.zipsoon.api.infrastructure.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final UserMapper mapper;
    
    public Optional<User> findById(Long id) {
        return mapper.selectById(id);
    }
    
    public Optional<User> findByEmail(String email) {
        return mapper.selectByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return mapper.existsByEmail(email);
    }
    
    public void save(User user) {
        mapper.insert(user);
    }
    
    public void delete(Long id) {
        mapper.delete(id);
    }
}
