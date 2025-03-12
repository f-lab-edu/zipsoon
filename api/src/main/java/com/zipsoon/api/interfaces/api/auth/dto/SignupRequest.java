package com.zipsoon.api.interfaces.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원가입 요청")
public record SignupRequest(
    @Schema(description = "사용자 이메일", example = "user@example.com")
    @NotBlank @Email String email,
    
    @Schema(description = "사용자 이름", example = "홍길동")
    @NotBlank String name
) {}
