package com.zipsoon.zipsoonbatch.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PropertyHistory {
    private Long id;
    private Long propertyId;
    private String changeType;
    private String beforeValue;
    private String afterValue;
    private LocalDateTime createdAt;
}