package com.zipsoon.batch.application.service.source.collector;

public interface SourceCollector {
    void create();
    boolean wasUpdated();
    void collect();
    void preprocess();
}