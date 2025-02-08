package com.zipsoon.api.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
    @NotBlank String email,
    @NotBlank String password
) {}