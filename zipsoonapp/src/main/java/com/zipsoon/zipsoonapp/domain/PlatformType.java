package com.zipsoon.zipsoonapp.domain;

public enum PlatformType {
    NAVER("네이버부동산");

    private final String description;

    PlatformType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

