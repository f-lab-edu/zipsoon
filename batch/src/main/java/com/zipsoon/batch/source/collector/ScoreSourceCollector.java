package com.zipsoon.batch.source.collector;

public interface ScoreSourceCollector {
    void create();
    boolean wasUpdated();
    void collect();
    void preprocess();
}