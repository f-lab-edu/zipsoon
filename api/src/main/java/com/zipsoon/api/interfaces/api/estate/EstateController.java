package com.zipsoon.api.interfaces.api.estate;

import com.zipsoon.api.application.estate.EstateService;
import com.zipsoon.api.application.estate.ScoreService;
import com.zipsoon.api.domain.auth.UserPrincipal;
import com.zipsoon.api.interfaces.api.estate.dto.EstateDetailResponse;
import com.zipsoon.api.interfaces.api.estate.dto.EstateResponse;
import com.zipsoon.api.interfaces.api.estate.dto.ScoreTypeResponse;
import com.zipsoon.api.interfaces.api.estate.dto.ViewportRequest;
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
public class EstateController {
    private final EstateService estateService;
    private final ScoreService scoreService;

    /**
     * 뷰포트 내의 매물 목록을 조회합니다.
     * 인증된 사용자의 경우 개인화된 점수 필터링을 적용합니다.
     *
     * @param request 뷰포트 요청 정보
     * @param userPrincipal 현재 로그인한 사용자 정보 (없으면 null)
     * @return 매물 목록 응답
     */
    @GetMapping("/map")
    public ResponseEntity<List<EstateResponse>> getEstatesInViewport(
        @Valid ViewportRequest request,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        
        return ResponseEntity.ok(
            estateService.findEstatesInViewport(request, userId)
        );
    }

    /**
     * 매물 상세 정보를 조회합니다.
     * 인증된 사용자의 경우 개인화된 점수 필터링을 적용합니다.
     *
     * @param id 매물 ID
     * @param userPrincipal 현재 로그인한 사용자 정보 (없으면 null)
     * @return 매물 상세 정보 응답
     */
    @GetMapping("/{id}")
    public ResponseEntity<EstateDetailResponse> getEstateDetail(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        
        return ResponseEntity.ok(
            estateService.findEstateDetail(id, userId)
        );
    }
    
    /**
     * 점수 유형 목록을 조회합니다.
     * 인증된 사용자는 개인화된 활성화 상태가 반영됩니다.
     *
     * @param userPrincipal 현재 로그인한 사용자 정보 (없으면 null)
     * @return 점수 유형 목록 응답
     */
    @GetMapping("/score-types")
    public ResponseEntity<List<ScoreTypeResponse>> getScoreTypes(
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        
        return ResponseEntity.ok(
            scoreService.getAllScoreTypes(userId)
        );
    }
    
    /**
     * 점수 유형을 비활성화합니다. 인증 필요.
     *
     * @param scoreTypeId 점수 유형 ID
     * @param userPrincipal 로그인한 사용자 정보
     * @return 성공 응답
     */
    @PostMapping("/score-types/{scoreTypeId}/disable")
    public ResponseEntity<Void> disableScoreType(
        @PathVariable Integer scoreTypeId,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        scoreService.disableScoreType(userPrincipal.getId(), scoreTypeId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 점수 유형을 활성화합니다. 인증 필요.
     *
     * @param scoreTypeId 점수 유형 ID
     * @param userPrincipal 로그인한 사용자 정보
     * @return 성공 응답
     */
    @PostMapping("/score-types/{scoreTypeId}/enable")
    public ResponseEntity<Void> enableScoreType(
        @PathVariable Integer scoreTypeId,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        scoreService.enableScoreType(userPrincipal.getId(), scoreTypeId);
        return ResponseEntity.ok().build();
    }

    /**
     * 매물 찜하기
     */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<Void> addFavorite(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        estateService.addFavorite(id, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 매물 찜하기 취소
     */
    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<Void> removeFavorite(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        estateService.removeFavorite(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
