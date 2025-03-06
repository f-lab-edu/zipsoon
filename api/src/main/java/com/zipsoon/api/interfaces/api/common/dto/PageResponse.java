package com.zipsoon.api.interfaces.api.common.dto;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    int totalElements,
    int totalPages
) {
    public PageResponse(List<T> content, int page, int size, int totalElements) {
        this(
            content,
            page,
            size,
            totalElements,
            calculateTotalPages(totalElements, size)
        );
    }

    private static int calculateTotalPages(int totalElements, int size) {
        return size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    }
}