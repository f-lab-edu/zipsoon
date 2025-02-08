package com.zipsoon.common.repository;

import com.zipsoon.common.domain.user.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(@Param("provider") String provider, @Param("providerId") String providerId);
    boolean existsByEmail(String email);
    void save(User user);
    void update(User user);
    void delete(Long id);
}
