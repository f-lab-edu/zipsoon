package com.zipsoon.batch.source.collector;

public interface ScoreSourceCollector {
    void create();
    void collect();
    boolean validate();
}
