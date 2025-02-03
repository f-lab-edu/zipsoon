package com.zipsoon.api.auth.controller;

import com.zipsoon.api.auth.dto.LoginRequest;
import com.zipsoon.api.auth.dto.SignupRequest;
import com.zipsoon.api.auth.service.UserService;
import com.zipsoon.common.security.dto.AuthToken;
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

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<AuthToken> signup(@Valid @RequestBody SignupRequest request) {
        AuthToken token = userService.signup(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthToken> login(@Valid @RequestBody LoginRequest request) {
        AuthToken token = userService.login(request);
        return ResponseEntity.ok(token);
    }
}
