package com.zipsoon.common.domain;

import lombok.Getter;

@Getter
public enum PlatformType {
    네이버("네이버"),
    직방("직방");

    private final String koreanName;

    PlatformType(String koreanName) {
        this.koreanName = koreanName;
    }

    public static PlatformType of(String koreanName) {
        for (PlatformType platformType: values()) {
            if (platformType.koreanName.equals(koreanName)) {
                return platformType;
            }
        }
        return null;
    }
}