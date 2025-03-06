package com.zipsoon.api.infrastructure.repository.user;

import com.zipsoon.api.domain.user.UserFavoriteEstate;
import com.zipsoon.api.interfaces.mapper.UserFavoriteEstateMapper;
import com.zipsoon.common.domain.Estate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class UserFavoriteEstateRepository {

    private final UserFavoriteEstateMapper mapper;

    public List<Estate> findFavoriteEstatesByUserId(Long userId, int offset, int size) {
        return mapper.findFavoriteEstatesByUserId(userId, offset, size);
    }

    public void addFavorite(UserFavoriteEstate userFavoriteEstate) {
        mapper.insertFavorite(userFavoriteEstate);
    }

    public void removeFavorite(Long userId, Long estateId) {
        mapper.deleteFavorite(userId, estateId);
    }

    public boolean existsByUserIdAndEstateId(Long userId, Long estateId) {
        return mapper.existsByUserIdAndEstateId(userId, estateId);
    }

    public int countByUserId(Long userId) {
        return mapper.countByUserId(userId);
    }
}