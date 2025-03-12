package com.zipsoon.api.interfaces.api.estate;

import com.zipsoon.api.application.estate.EstateService;
import com.zipsoon.api.application.estate.ScoreService;
import com.zipsoon.api.domain.auth.UserPrincipal;
import com.zipsoon.api.interfaces.api.estate.dto.EstateDetailResponse;
import com.zipsoon.api.interfaces.api.estate.dto.EstateResponse;
import com.zipsoon.api.interfaces.api.estate.dto.ScoreTypeResponse;
import com.zipsoon.api.interfaces.api.estate.dto.ViewportRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/estates")
@RequiredArgsConstructor
@Tag(name = "매물", description = "부동산 매물 조회 및 관리 API")
public class EstateController {
    private final EstateService estateService;
    private final ScoreService scoreService;

    @Operation(
        summary = "지도 뷰포트 내 매물 목록 조회",
        description = "지정된 지도 영역 내의 매물 목록을 조회합니다. 인증된 사용자의 경우 개인화된 점수 필터링을 적용합니다.",
        security = @SecurityRequirement(name = "JWT_ACCESS_TOKEN")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "매물 목록 조회 성공", 
                content = @Content(schema = @Schema(implementation = EstateResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("/map")
    public ResponseEntity<List<EstateResponse>> getEstatesInViewport(
        @Parameter(description = "지도 뷰포트 좌표 정보") @Valid ViewportRequest request,
        @Parameter(description = "현재 로그인한 사용자 정보 (선택적)") @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        
        return ResponseEntity.ok(
            estateService.findEstatesInViewport(request, userId)
        );
    }

    @Operation(
        summary = "매물 상세 정보 조회",
        description = "특정 매물의 상세 정보를 조회합니다. 인증된 사용자의 경우 개인화된 점수 필터링을 적용합니다.",
        security = @SecurityRequirement(name = "JWT_ACCESS_TOKEN")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "매물 상세 정보 조회 성공", 
                content = @Content(schema = @Schema(implementation = EstateDetailResponse.class))),
        @ApiResponse(responseCode = "404", description = "매물을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EstateDetailResponse> getEstateDetail(
        @Parameter(description = "매물 ID") @PathVariable Long id,
        @Parameter(description = "현재 로그인한 사용자 정보 (선택적)") @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        
        return ResponseEntity.ok(
            estateService.findEstateDetail(id, userId)
        );
    }
    
    @Operation(
        summary = "점수 유형 목록 조회",
        description = "매물 평가에 사용되는 점수 유형 목록을 조회합니다. 인증된 사용자는 개인화된 활성화 상태가 반영됩니다.",
        security = @SecurityRequirement(name = "JWT_ACCESS_TOKEN")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "점수 유형 목록 조회 성공", 
                content = @Content(schema = @Schema(implementation = ScoreTypeResponse.class)))
    })
    @GetMapping("/score-types")
    public ResponseEntity<List<ScoreTypeResponse>> getScoreTypes(
        @Parameter(description = "현재 로그인한 사용자 정보 (선택적)") @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        
        return ResponseEntity.ok(
            scoreService.getAllScoreTypes(userId)
        );
    }
    
    @Operation(
        summary = "점수 유형 비활성화",
        description = "특정 점수 유형을 사용자별로 비활성화합니다. 이후 매물 점수 계산에서 제외됩니다.",
        security = @SecurityRequirement(name = "JWT_ACCESS_TOKEN")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "점수 유형 비활성화 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 점수 유형")
    })
    @PostMapping("/score-types/{scoreTypeId}/disable")
    public ResponseEntity<Void> disableScoreType(
        @Parameter(description = "비활성화할 점수 유형 ID") @PathVariable Integer scoreTypeId,
        @Parameter(description = "현재 로그인한 사용자 정보") @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        scoreService.disableScoreType(userPrincipal.getId(), scoreTypeId);
        return ResponseEntity.ok().build();
    }
    
    @Operation(
        summary = "점수 유형 활성화",
        description = "특정 점수 유형을 사용자별로 활성화합니다. 이후 매물 점수 계산에 포함됩니다.",
        security = @SecurityRequirement(name = "JWT_ACCESS_TOKEN")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "점수 유형 활성화 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 점수 유형")
    })
    @PostMapping("/score-types/{scoreTypeId}/enable")
    public ResponseEntity<Void> enableScoreType(
        @Parameter(description = "활성화할 점수 유형 ID") @PathVariable Integer scoreTypeId,
        @Parameter(description = "현재 로그인한 사용자 정보") @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        scoreService.enableScoreType(userPrincipal.getId(), scoreTypeId);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "매물 찜하기",
        description = "사용자가 특정 매물을 관심 매물로 등록합니다.",
        security = @SecurityRequirement(name = "JWT_ACCESS_TOKEN")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "매물 찜하기 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 매물"),
        @ApiResponse(responseCode = "409", description = "이미 찜한 매물")
    })
    @PostMapping("/{id}/favorite")
    public ResponseEntity<Void> addFavorite(
        @Parameter(description = "찜할 매물 ID") @PathVariable Long id,
        @Parameter(description = "현재 로그인한 사용자 정보") @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        estateService.addFavorite(id, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
        summary = "매물 찜하기 취소",
        description = "사용자가 관심 매물에서 특정 매물을 제거합니다.",
        security = @SecurityRequirement(name = "JWT_ACCESS_TOKEN")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "매물 찜하기 취소 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 매물 또는 찜하지 않은 매물")
    })
    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<Void> removeFavorite(
        @Parameter(description = "찜하기 취소할 매물 ID") @PathVariable Long id,
        @Parameter(description = "현재 로그인한 사용자 정보") @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        estateService.removeFavorite(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
