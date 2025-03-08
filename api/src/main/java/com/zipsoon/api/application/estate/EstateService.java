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
        // Entry logging - DEBUG level
        log.debug("[SVC:IN] findEstatesInViewport(viewport={}, userId={})", 
                formatViewport(request), userId != null ? userId : "guest");
        
        if (request.swLng() >= request.neLng() || request.swLat() >= request.neLat()) {
            log.warn("[SVC:ERR] 유효하지 않은 뷰포트 좌표: {}", formatViewport(request));
            throw new ServiceException(ErrorCode.BAD_REQUEST, "뷰포트 좌표가 유효하지 않습니다.");
        }

        // 조회 개수 제한 계산
        var limit = calculateResultLimit(request.zoom());
        log.debug("[SVC:PARAM] 뷰포트 내 매물 검색 제한: {}", limit);

        // 매물 조회
        var estates = apiEstateRepository.findAllInViewport(request, limit);

        // 조회 결과가 없는 경우
        if (estates.isEmpty()) {
            log.info("[SVC:RESULT] 뷰포트 내 매물 없음: {}", formatViewport(request));
            
            // Exit logging - DEBUG level
            log.debug("[SVC:OUT] findEstatesInViewport() 완료 - 반환된 매물 수: 0");
            return Collections.emptyList();
        }

        log.info("[SVC:RESULT] 뷰포트 내 {} 개의 매물 발견 (userId: {})", 
                estates.size(), userId != null ? userId : "guest");

        // 모든 매물에 점수 정보 추가하여 응답 생성
        var estateResponses = estates.stream()
            .map(estate -> {
                try {
                    ScoreSummaryResponse scoreSummary = scoreService.getScoreSummary(estate.getId(), userId);
                    return EstateResponse.from(estate, scoreSummary);
                } catch (Exception e) {
                    log.error("[SVC:ERR] 매물 {} 점수 계산 오류: {}", estate.getId(), e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();
            
        // Exit logging - DEBUG level
        log.debug("[SVC:OUT] findEstatesInViewport() 완료 - 반환된 매물 수: {}", estateResponses.size());
        
        return estateResponses;
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
        log.debug("[SVC:IN] findEstateDetail(id={}, userId={})", id, userId != null ? userId : "guest");

        // 매물 조회
        Estate estate = apiEstateRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("[SVC:ERR] ID가 {}인 매물을 찾을 수 없음", id);
                return new ServiceException(ErrorCode.ESTATE_NOT_FOUND);
            });
        
        log.debug("[SVC:RESULT] 매물 발견 ID: {}, 주소: {}", id, estate.getAddress());
            
        // 찜 상태 확인
        var isFavorite = false;
        if (userId != null) {
            isFavorite = userFavoriteEstateRepository.existsByUserIdAndEstateId(userId, id);
            log.debug("[SVC:PARAM] 사용자 {} 찜 상태: {}", userId, isFavorite);
        }

        // 매물 점수 정보 추가하여 응답 생성
        try {
            var scoreDetails = scoreService.getScoreDetails(estate.getId(), userId);
            var response = EstateDetailResponse.from(estate, scoreDetails, isFavorite);
            log.debug("[SVC:OUT] findEstateDetail() 완료 - 매물 ID: {}, 총점: {}", 
                     id, scoreDetails.totalScore());
            return response;
        } catch (Exception e) {
            log.error("[SVC:ERR] 매물 {} 상세 점수 계산 오류: {}", estate.getId(), e.getMessage());
            // 점수 정보 없이 기본 상세 정보 반환
            var emptyScoreDetails = new ScoreDetailsResponse(0.0, "점수 정보를 조회할 수 없습니다", List.of());
            var response = EstateDetailResponse.from(estate, emptyScoreDetails, isFavorite);
            log.debug("[SVC:OUT] findEstateDetail() 완료 - 매물 ID: {} (점수 없음)", id);
            return response;
        }
    }

    @Transactional
    public void addFavorite(Long estateId, Long userId) {
        log.debug("[SVC:IN] addFavorite(estateId={}, userId={})", estateId, userId);
        
        // 매물 존재 여부 확인
        apiEstateRepository.findById(estateId)
            .orElseThrow(() -> {
                log.warn("[SVC:ERR] 찜하기 실패 - ID가 {}인 매물을 찾을 수 없음", estateId);
                return new ServiceException(ErrorCode.ESTATE_NOT_FOUND);
            });

        // 이미 찜한 매물인지 확인
        if (userFavoriteEstateRepository.existsByUserIdAndEstateId(userId, estateId)) {
            log.info("[SVC:RESULT] 이미 찜한 매물임: estateId={}, userId={}", estateId, userId);
            log.debug("[SVC:OUT] addFavorite() 완료 - 이미 찜한 매물");
            return; // 이미 찜한 경우 아무 작업 없이 반환
        }

        // 찜하기 추가
        var favorite = UserFavoriteEstate.of(userId, estateId);
        userFavoriteEstateRepository.save(favorite);
        
        log.info("[SVC:RESULT] 매물 찜하기 추가 성공: estateId={}, userId={}", estateId, userId);
        log.debug("[SVC:OUT] addFavorite() 완료");
    }

    @Transactional
    public void removeFavorite(Long estateId, Long userId) {
        log.debug("[SVC:IN] removeFavorite(estateId={}, userId={})", estateId, userId);
        
        // 매물 존재 여부 확인
        apiEstateRepository.findById(estateId)
            .orElseThrow(() -> {
                log.warn("[SVC:ERR] 찜하기 삭제 실패 - ID가 {}인 매물을 찾을 수 없음", estateId);
                return new ServiceException(ErrorCode.ESTATE_NOT_FOUND);
            });

        // 찜하기 있는지 확인
        boolean exists = userFavoriteEstateRepository.existsByUserIdAndEstateId(userId, estateId);
        
        // 찜하기 삭제
        userFavoriteEstateRepository.delete(userId, estateId);
        
        if (exists) {
            log.info("[SVC:RESULT] 매물 찜하기 삭제 성공: estateId={}, userId={}", estateId, userId);
        } else {
            log.info("[SVC:RESULT] 찜하기 삭제 대상 없음: estateId={}, userId={}", estateId, userId);
        }
        
        log.debug("[SVC:OUT] removeFavorite() 완료");
    }

    @Transactional(readOnly = true)
    public PageResponse<EstateResponse> findFavoriteEstates(Long userId, int page, int size) {
        log.debug("[SVC:IN] findFavoriteEstates(userId={}, page={}, size={})", userId, page, size);
        
        // 페이지네이션 계산 (page는 1부터 시작)
        var offset = (page - 1) * size;
        log.debug("[SVC:PARAM] 페이지네이션 계산: offset={}, size={}", offset, size);

        // 찜한 매물 목록 조회
        var favorites = userFavoriteEstateRepository.findFavoriteEstatesByUserId(userId, offset, size);

        // 총 개수 조회
        var total = userFavoriteEstateRepository.countByUserId(userId);
        
        log.info("[SVC:RESULT] 찜한 매물 목록 조회 결과: 페이지 항목 {}개, 총 {}개", 
                favorites.size(), total);

        // 응답 데이터 변환 (점수 정보 포함)
        var responseList = favorites.stream()
            .map(estate -> {
                try {
                    var scoreSummary = scoreService.getScoreSummary(estate.getId(), userId);
                    return EstateResponse.from(estate, scoreSummary);
                } catch (Exception e) {
                    log.error("[SVC:ERR] 매물 {} 점수 계산 오류: {}", estate.getId(), e.getMessage());
                    // 점수 정보 없이 기본 매물 정보 반환
                    var emptySummary = new ScoreSummaryResponse(0.0, "점수 정보 없음");
                    return EstateResponse.from(estate, emptySummary);
                }
            })
            .toList();
        
        var response = new PageResponse<>(responseList, page, size, total);
        log.debug("[SVC:OUT] findFavoriteEstates() 완료 - 페이지 {}의 {}개 항목 반환", page, responseList.size());
        
        return response;
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