package com.zipsoon.common.domain.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(@Param("provider") String provider, @Param("providerId") String providerId);
    boolean existsByEmail(String email);
    void save(User user);
    void update(User user);
    void delete(Long id);
}
