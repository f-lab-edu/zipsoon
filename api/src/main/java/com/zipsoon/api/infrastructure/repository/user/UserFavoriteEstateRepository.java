package com.zipsoon.api.infrastructure.repository.user;

import com.zipsoon.api.domain.user.UserFavoriteEstate;
import com.zipsoon.api.infrastructure.mapper.user.UserFavoriteEstateMapper;
import com.zipsoon.common.domain.Estate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class UserFavoriteEstateRepository {

    private final UserFavoriteEstateMapper mapper;

    public List<Estate> findFavoriteEstatesByUserId(Long userId, int offset, int size) {
        return mapper.selectFavoriteEstatesByUserId(userId, offset, size);
    }

    public void save(UserFavoriteEstate userFavoriteEstate) {
        mapper.insert(userFavoriteEstate);
    }

    public void delete(Long userId, Long estateId) {
        mapper.delete(userId, estateId);
    }

    public boolean existsByUserIdAndEstateId(Long userId, Long estateId) {
        return mapper.existsByUserIdAndEstateId(userId, estateId);
    }

    public int countByUserId(Long userId) {
        return mapper.countByUserId(userId);
    }
}