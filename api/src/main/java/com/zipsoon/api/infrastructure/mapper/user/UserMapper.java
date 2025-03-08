package com.zipsoon.api.infrastructure.mapper.user;

import com.zipsoon.api.domain.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {
    Optional<User> selectById(Long id);
    Optional<User> selectByEmail(String email);
    Optional<User> selectByProviderAndProviderId(
        @Param("provider") String provider,
        @Param("providerId") String providerId
    );
    boolean existsByEmail(String email);
    void insert(User user);
    void delete(Long id);
}