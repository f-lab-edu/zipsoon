package com.zipsoon.api.application.estate;

import com.zipsoon.api.infrastructure.repository.estate.ApiEstateRepository;
import com.zipsoon.api.infrastructure.exception.custom.ServiceException;
import com.zipsoon.api.infrastructure.exception.model.ErrorCode;
import com.zipsoon.api.interfaces.api.estate.dto.*;
import com.zipsoon.common.domain.Estate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.zipsoon.api.infrastructure.exception.model.ErrorCode.ESTATE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstateService {
    private final ScoreService scoreService;
    private final ApiEstateRepository apiEstateRepository;
    private static final int MAX_RESULTS_PER_ZOOM = 1000;

    /**
     * 뷰포트 내의 매물 목록을 조회합니다. 사용자가 로그인한 경우, 사용자별 점수 선호도를 반영합니다.
     *
     * @param request 뷰포트 요청 정보
     * @param userId 사용자 ID (로그인한 경우에만 제공)
     * @return 매물 응답 목록
     */
    @Transactional(readOnly = true)
    public List<EstateResponse> findEstatesInViewport(ViewportRequest request, Long userId) {
        int limit = calculateLimit(request.zoom());
        List<Estate> estates = apiEstateRepository.findAllInViewport(request, limit);

        if (estates.isEmpty()) {
            throw new ServiceException(ErrorCode.ESTATE_NOT_FOUND, "해당 지역에 매물이 없습니다.");
        }

        return estates.stream()
            .map(estate -> {
                try {
                    // 로그인한 사용자인 경우 사용자별 점수 설정 반영
                    ScoreSummary scoreSummary = scoreService.getScoreSummary(estate.getId(), userId);
                    return EstateResponse.from(estate, scoreSummary);
                } catch (Exception e) {
                    log.error("Error processing estate ID {}: {}", estate.getId(), e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();
    }
    
    /**
     * 매물 상세 정보를 조회합니다. 사용자가 로그인한 경우, 사용자별 점수 선호도를 반영합니다.
     *
     * @param id 매물 ID
     * @param userId 사용자 ID (로그인한 경우에만 제공)
     * @return 매물 상세 응답
     */
    @Transactional(readOnly = true)
    public EstateDetailResponse findEstateDetail(Long id, Long userId) {
        Estate estate = apiEstateRepository.findById(id)
            .orElseThrow(() -> new ServiceException(ESTATE_NOT_FOUND));

        try {
            // 로그인한 사용자인 경우 사용자별 점수 설정 반영
            ScoreDetails scoreDetails = scoreService.getScoreDetails(estate.getId(), userId);
            return EstateDetailResponse.from(estate, scoreDetails);
        } catch (Exception e) {
            log.error("Error processing estate detail ID {}: {}", id, e.getMessage());
            ScoreDetails emptyScoreDetails = new ScoreDetails(0.0, "점수 정보가 없습니다", List.of());
            return EstateDetailResponse.from(estate, emptyScoreDetails);
        }
    }

    private int calculateLimit(int zoom) {
        if (zoom >= 14) return 500;
        return MAX_RESULTS_PER_ZOOM;
    }
}
