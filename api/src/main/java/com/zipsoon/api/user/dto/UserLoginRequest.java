package com.zipsoon.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
    @NotBlank String email,
    @NotBlank String password
) {}