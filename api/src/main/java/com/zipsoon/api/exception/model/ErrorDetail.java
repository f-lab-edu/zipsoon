package com.zipsoon.api.exception.model;

public record ErrorDetail(
    String field,
    String message,
    String code
) {}