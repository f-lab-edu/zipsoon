package com.zipsoon.api.interfaces.api.auth;

import com.zipsoon.api.application.auth.AuthService;
import com.zipsoon.api.interfaces.api.auth.dto.AuthTokenResponse;
import com.zipsoon.api.interfaces.api.auth.dto.LoginRequest;
import com.zipsoon.api.interfaces.api.auth.dto.SignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "회원가입, 로그인 등 인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록하고 인증 토큰을 발급합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공", 
                    content = @Content(schema = @Schema(implementation = AuthTokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
    @PostMapping("/signup")
    public ResponseEntity<AuthTokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthTokenResponse token = authService.signup(request);
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 인증 토큰을 발급합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공", 
                    content = @Content(schema = @Schema(implementation = AuthTokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthTokenResponse token = authService.login(request);
        return ResponseEntity.ok(token);
    }

}
