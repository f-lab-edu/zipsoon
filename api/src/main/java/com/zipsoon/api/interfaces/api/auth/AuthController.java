package com.zipsoon.api.interfaces.api.auth;

import com.zipsoon.api.interfaces.api.auth.dto.AuthToken;
import com.zipsoon.api.interfaces.api.auth.dto.LoginRequest;
import com.zipsoon.api.interfaces.api.auth.dto.SignupRequest;
import com.zipsoon.api.application.auth.AuthService;
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
    public ResponseEntity<AuthToken> signup(@Valid @RequestBody SignupRequest request) {
        AuthToken token = authService.signup(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthToken> login(@Valid @RequestBody LoginRequest request) {
        AuthToken token = authService.login(request);
        return ResponseEntity.ok(token);
    }

}
