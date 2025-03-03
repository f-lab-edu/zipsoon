package com.zipsoon.api.interfaces.api.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
    @NotBlank String name,
    String imageUrl
) {}
