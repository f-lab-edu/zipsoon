package com.zipsoon.batch.application.service.estate.collector;

import com.zipsoon.common.domain.Estate;

import java.util.List;

public interface EstateCollector {
    String getPlatformName();
    List<Estate> collect(String dongCode, int page);
    boolean hasMoreData(String dongCode, int page);
}
