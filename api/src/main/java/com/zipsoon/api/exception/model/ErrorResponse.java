package com.zipsoon.api.exception.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    int code,
    String message,
    String requestId,
    LocalDateTime timestamp,
    List<ErrorDetail> details
) {}