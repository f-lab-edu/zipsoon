package com.zipsoon.api.interfaces.api.auth;

import com.zipsoon.api.application.auth.AuthService;
import com.zipsoon.api.interfaces.api.auth.dto.AuthTokenResponse;
import com.zipsoon.api.interfaces.api.auth.dto.LoginRequest;
import com.zipsoon.api.interfaces.api.auth.dto.SignupRequest;
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
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthTokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthTokenResponse token = authService.signup(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthTokenResponse token = authService.login(request);
        return ResponseEntity.ok(token);
    }

}
