package com.zipsoon.api.interfaces.mapper;

import com.zipsoon.api.domain.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(
        @Param("provider") String provider,
        @Param("providerId") String providerId
    );
    boolean existsByEmail(String email);
    void save(User user);
    void delete(Long id);
}