package com.zipsoon.zipsoonbatch.repository.mapper;

import com.zipsoon.zipsoonbatch.domain.PropertyHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PropertyHistoryMapper {
    void save(PropertyHistory propertyHistory);
}