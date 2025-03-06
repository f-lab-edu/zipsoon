package com.zipsoon.api.interfaces.mapper;

import com.zipsoon.api.domain.user.UserFavoriteEstate;
import com.zipsoon.common.domain.Estate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFavoriteEstateMapper {
    List<Estate> findFavoriteEstatesByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    void insertFavorite(@Param("userFavoriteEstate") UserFavoriteEstate userFavoriteEstate);
    void deleteFavorite(@Param("userId") Long userId, @Param("estateId") Long estateId);
    boolean existsByUserIdAndEstateId(@Param("userId") Long userId, @Param("estateId") Long estateId);
    int countByUserId(@Param("userId") Long userId);
}