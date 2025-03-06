package com.zipsoon.api.interfaces.api.user;

import com.zipsoon.api.application.estate.EstateService;
import com.zipsoon.api.application.user.UserService;
import com.zipsoon.api.domain.auth.UserPrincipal;
import com.zipsoon.api.interfaces.api.common.dto.PageResponse;
import com.zipsoon.api.interfaces.api.estate.dto.EstateResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    private final EstateService estateService;


    /**
     * 내가 찜한 매물 목록 조회
     */
    @GetMapping("/me/favorites")
    public ResponseEntity<PageResponse<EstateResponse>> getFavorites(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
    ) {
        PageResponse<EstateResponse> favorites = estateService.findFavoriteEstates(
            userPrincipal.getId(), page, size);
        return ResponseEntity.ok(favorites);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        userService.deleteAccount(principal.getId());
        return ResponseEntity.noContent().build();
    }

}
