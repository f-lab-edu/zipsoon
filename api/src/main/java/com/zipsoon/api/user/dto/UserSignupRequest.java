package com.zipsoon.api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserSignupRequest(
    @NotBlank @Email String email,
    @NotBlank String password,
    @NotBlank String name
) {}
