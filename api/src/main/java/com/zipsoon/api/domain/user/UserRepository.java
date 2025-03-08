package com.zipsoon.api.domain.user;

import java.util.Optional;

/**
 * 사용자 정보에 접근하고 관리하는 리포지토리 인터페이스입니다.
 */
public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    void save(User user);
    void delete(Long id);
}