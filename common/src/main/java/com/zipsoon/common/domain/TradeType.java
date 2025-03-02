package com.zipsoon.common.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TradeType {
    A1("매매"),
    B1("전세"),
    B2("월세"),
    B3("단기임대");

    private final String koreanName;

    TradeType(String koreanName) {
        this.koreanName = koreanName;
    }

    @JsonValue
    public String getKoreanName() {
        return koreanName;
    }

    public static TradeType of(String koreanName) {
        for (TradeType tradeType: values()) {
            if (tradeType.koreanName.equals(koreanName)) {
                return tradeType;
            }
        }
        return null;
    }
}