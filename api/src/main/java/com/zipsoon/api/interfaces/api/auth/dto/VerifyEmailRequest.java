package com.zipsoon.api.interfaces.api.auth.dto;

public record VerifyEmailRequest(
    String email,
    String verificationCode
) {}