package com.zipsoon.api.interfaces.api.user;

import com.zipsoon.api.application.estate.EstateService;
import com.zipsoon.api.application.user.UserService;
import com.zipsoon.api.domain.auth.UserPrincipal;
import com.zipsoon.api.interfaces.api.common.dto.PageResponse;
import com.zipsoon.api.interfaces.api.estate.dto.EstateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "사용자", description = "사용자 정보 관리 API")
public class UserController {

    private final UserService userService;

    private final EstateService estateService;


    @Operation(
        summary = "찜한 매물 목록 조회",
        description = "사용자가 찜한 매물 목록을 페이지네이션하여 조회합니다.",
        security = @SecurityRequirement(name = "JWT_ACCESS_TOKEN")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "찜한 매물 목록 조회 성공", 
                content = @Content(schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/favorites")
    public ResponseEntity<PageResponse<EstateResponse>> getFavorites(
        @Parameter(description = "현재 로그인한 사용자 정보") @AuthenticationPrincipal UserPrincipal userPrincipal,
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") @Min(0) int page,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
    ) {
        PageResponse<EstateResponse> favorites = estateService.findFavoriteEstates(
            userPrincipal.getId(), page, size);
        return ResponseEntity.ok(favorites);
    }

    @Operation(
        summary = "회원 탈퇴",
        description = "사용자 계정을 탈퇴 처리합니다. 관련 개인정보 및 설정이 삭제됩니다.",
        security = @SecurityRequirement(name = "JWT_ACCESS_TOKEN")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
        @Parameter(description = "현재 로그인한 사용자 정보") @AuthenticationPrincipal UserPrincipal principal
    ) {
        userService.deleteAccount(principal.getId());
        return ResponseEntity.noContent().build();
    }

}
