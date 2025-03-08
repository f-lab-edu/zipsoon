package com.zipsoon.api.application.estate;

import com.zipsoon.api.domain.user.UserFavoriteEstate;
import com.zipsoon.api.infrastructure.exception.custom.ServiceException;
import com.zipsoon.api.infrastructure.exception.model.ErrorCode;
import com.zipsoon.api.infrastructure.repository.estate.ApiEstateRepository;
import com.zipsoon.api.infrastructure.repository.user.UserFavoriteEstateRepository;
import com.zipsoon.api.interfaces.api.common.dto.PageResponse;
import com.zipsoon.api.interfaces.api.estate.dto.*;
import com.zipsoon.common.domain.Estate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstateService {
    private final ScoreService scoreService;
    private final ApiEstateRepository apiEstateRepository;
    private final UserFavoriteEstateRepository userFavoriteEstateRepository;
    private static final int MAX_RESULTS_PER_ZOOM = 1000;
    private static final int MAX_RESULTS_HIGH_ZOOM = 500;

    /**
     * 뷰포트 내의 매물 목록을 조회합니다.
     *
     * @param request 뷰포트 요청 정보
     * @param userId 사용자 ID (로그인한 경우에만 제공)
     * @return 매물 응답 목록
     * @throws ServiceException 매물 조회 중 오류 발생 시
     */
    @Transactional(readOnly = true)
    public List<EstateResponse> findEstatesInViewport(ViewportRequest request, Long userId) {
        if (request.swLng() >= request.neLng() || request.swLat() >= request.neLat()) {
            log.warn("Invalid viewport coordinates: {}", formatViewport(request));
            throw new ServiceException(ErrorCode.BAD_REQUEST, "뷰포트 좌표가 유효하지 않습니다.");
        }

        // 조회 개수 제한 계산
        var limit = calculateResultLimit(request.zoom());
        log.debug("Searching estates in viewport with limit: {}", limit);

        // 매물 조회
        var estates = apiEstateRepository.findAllInViewport(request, limit);

        // 조회 결과가 없는 경우
        if (estates.isEmpty()) {
            log.info("No estates found in viewport: {}", formatViewport(request));
            return Collections.emptyList();
        }

        log.info("Found {} estates in viewport (userId: {})", estates.size(), userId != null ? userId : "guest");

        // 모든 매물에 점수 정보 추가하여 응답 생성
        return estates.stream()
            .map(estate -> {
                try {
                    ScoreSummaryResponse scoreSummary = scoreService.getScoreSummary(estate.getId(), userId);
                    return EstateResponse.from(estate, scoreSummary);
                } catch (Exception e) {
                    log.error("Error calculating scores for estate {}: {}", estate.getId(), e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * 매물 상세 정보를 조회합니다.
     *
     * @param id 매물 ID
     * @param userId 사용자 ID (로그인한 경우에만 제공)
     * @return 매물 상세 응답
     * @throws ServiceException 매물이 존재하지 않거나 접근할 수 없는 경우
     */
    @Transactional(readOnly = true)
    public EstateDetailResponse findEstateDetail(Long id, Long userId) {
        log.debug("Finding estate detail for id: {} (userId: {})", id, userId != null ? userId : "guest");

        // 매물 조회
        Estate estate = apiEstateRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Estate not found with id: {}", id);
                return new ServiceException(ErrorCode.ESTATE_NOT_FOUND);
            });
            
        // 찜 상태 확인
        var isFavorite = false;
        if (userId != null) {
            isFavorite = userFavoriteEstateRepository.existsByUserIdAndEstateId(userId, id);
        }

        // 매물 점수 정보 추가하여 응답 생성
        try {
            var scoreDetails = scoreService.getScoreDetails(estate.getId(), userId);
            return EstateDetailResponse.from(estate, scoreDetails, isFavorite);
        } catch (Exception e) {
            log.error("Error calculating detailed scores for estate {}: {}", estate.getId(), e.getMessage());
            // 점수 정보 없이 기본 상세 정보 반환
            var emptyScoreDetails = new ScoreDetailsResponse(0.0, "점수 정보를 조회할 수 없습니다", List.of());
            return EstateDetailResponse.from(estate, emptyScoreDetails, isFavorite);
        }
    }

    @Transactional
    public void addFavorite(Long estateId, Long userId) {
        // 매물 존재 여부 확인
        apiEstateRepository.findById(estateId)
            .orElseThrow(() -> new ServiceException(ErrorCode.ESTATE_NOT_FOUND));

        // 이미 찜한 매물인지 확인
        if (userFavoriteEstateRepository.existsByUserIdAndEstateId(userId, estateId)) {
            return; // 이미 찜한 경우 아무 작업 없이 반환
        }

        // 찜하기 추가
        var favorite = UserFavoriteEstate.create(userId, estateId);
        userFavoriteEstateRepository.addFavorite(favorite);
    }

    @Transactional
    public void removeFavorite(Long estateId, Long userId) {
        // 매물 존재 여부 확인
        apiEstateRepository.findById(estateId)
            .orElseThrow(() -> new ServiceException(ErrorCode.ESTATE_NOT_FOUND));

        // 찜하기 삭제
        userFavoriteEstateRepository.removeFavorite(userId, estateId);
    }

    @Transactional(readOnly = true)
    public PageResponse<EstateResponse> findFavoriteEstates(Long userId, int page, int size) {
        // 페이지네이션 계산 (page는 1부터 시작)
        var offset = (page - 1) * size;

        // 찜한 매물 목록 조회
        var favorites = userFavoriteEstateRepository.findFavoriteEstatesByUserId(userId, offset, size);

        // 총 개수 조회
        var total = userFavoriteEstateRepository.countByUserId(userId);

        // 응답 데이터 변환 (점수 정보 포함)
        var responseList = favorites.stream()
            .map(estate -> {
                var scoreSummary = scoreService.getScoreSummary(estate.getId(), userId);
                return EstateResponse.from(estate, scoreSummary);
            })
            .toList();

        return new PageResponse<>(responseList, page, size, total);
    }

    /**
     * 뷰포트 로깅 문자열 포맷
     */
    private String formatViewport(ViewportRequest request) {
        return String.format("sw(%f, %f), ne(%f, %f), zoom: %d",
            request.swLng(), request.swLat(), request.neLng(), request.neLat(), request.zoom());
    }

    /**
     * 확대 레벨에 따른 결과 제한 개수 계산
     */
    private int calculateResultLimit(int zoom) {
        if (zoom >= 14) return MAX_RESULTS_HIGH_ZOOM;
        return MAX_RESULTS_PER_ZOOM;
    }
}