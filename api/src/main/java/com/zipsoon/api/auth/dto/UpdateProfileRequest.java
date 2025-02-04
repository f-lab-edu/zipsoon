package com.zipsoon.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
    @NotBlank String name,
    String imageUrl
) {}
