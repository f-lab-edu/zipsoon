package com.zipsoon.api.user.controller;

import com.zipsoon.api.auth.model.UserPrincipal;
import com.zipsoon.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        userService.deleteAccount(principal.getId());
        return ResponseEntity.noContent().build();
    }

}
