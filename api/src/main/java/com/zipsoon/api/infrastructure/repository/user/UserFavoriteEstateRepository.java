package com.zipsoon.api.infrastructure.repository.user;

import com.zipsoon.api.domain.user.UserFavoriteEstate;
import com.zipsoon.common.domain.Estate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFavoriteEstateRepository {
    List<Estate> findFavoriteEstatesByUserId(Long userId, int page, int size);
    void addFavorite(UserFavoriteEstate userFavoriteEstate);
    void removeFavorite(Long userId, Long estateId);
    boolean existsByUserIdAndEstateId(Long userId, Long estateId);
    int countByUserId(Long userId);
}