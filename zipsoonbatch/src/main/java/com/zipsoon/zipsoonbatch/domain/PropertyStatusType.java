package com.zipsoon.zipsoonbatch.domain;

public enum PropertyStatusType {
    ACTIVE("활성"),
    DELETED("삭제됨");

    private final String description;

    PropertyStatusType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
