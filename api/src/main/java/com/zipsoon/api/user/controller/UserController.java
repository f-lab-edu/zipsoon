package com.zipsoon.api.user.controller;

import com.zipsoon.api.user.dto.*;
import com.zipsoon.api.user.dto.UserLoginRequest;
import com.zipsoon.api.user.dto.UserProfileResponse;
import com.zipsoon.api.user.service.UserService;
import com.zipsoon.common.security.dto.AuthToken;
import com.zipsoon.common.security.model.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<AuthToken> signup(@Valid @RequestBody UserSignupRequest request) {
        AuthToken token = userService.signup(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthToken> login(@Valid @RequestBody UserLoginRequest request) {
        AuthToken token = userService.login(request);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(userService.getProfile(principal.getId()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfile(principal.getId(), request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        userService.deleteAccount(principal.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(principal.getId(), request);
        return ResponseEntity.ok().build();
    }

}
