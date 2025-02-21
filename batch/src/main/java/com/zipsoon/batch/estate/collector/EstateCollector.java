package com.zipsoon.batch.estate.collector;

import com.zipsoon.common.domain.EstateSnapshot;

import java.util.List;

public interface EstateCollector {
    String getPlatformName();
    List<EstateSnapshot> collect(String dongCode, int page);
    boolean hasMoreData(String dongCode, int page);
}
