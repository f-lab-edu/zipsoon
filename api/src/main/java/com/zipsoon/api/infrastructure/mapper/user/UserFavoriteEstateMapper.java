package com.zipsoon.api.infrastructure.mapper.user;

import com.zipsoon.api.domain.user.UserFavoriteEstate;
import com.zipsoon.common.domain.Estate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFavoriteEstateMapper {
    List<Estate> selectFavoriteEstatesByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    void insert(@Param("userFavoriteEstate") UserFavoriteEstate userFavoriteEstate);
    void delete(@Param("userId") Long userId, @Param("estateId") Long estateId);
    boolean existsByUserIdAndEstateId(@Param("userId") Long userId, @Param("estateId") Long estateId);
    int countByUserId(@Param("userId") Long userId);
}