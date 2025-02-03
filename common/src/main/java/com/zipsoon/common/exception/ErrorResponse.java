package com.zipsoon.common.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    int code,
    String message,
    String requestId,
    LocalDateTime timestamp,
    Map<String, Object> data
) {
    @JsonCreator
    public ErrorResponse {
    }
}