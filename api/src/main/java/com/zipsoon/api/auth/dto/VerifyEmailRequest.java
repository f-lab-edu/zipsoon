package com.zipsoon.api.auth.dto;

public record VerifyEmailRequest(
    String email,
    String verificationCode
) {}