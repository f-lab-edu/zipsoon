package com.zipsoon.api.infrastructure.exception.model;

public record ErrorDetail(
    String field,
    String message,
    String code
) {}