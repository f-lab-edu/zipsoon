package com.zipsoon.api.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
    @NotBlank String name,
    String imageUrl
) {}
