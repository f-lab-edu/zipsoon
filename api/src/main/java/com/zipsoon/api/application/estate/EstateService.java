package com.zipsoon.api.application.estate;

import com.zipsoon.api.infrastructure.repository.estate.ApiEstateRepository;
import com.zipsoon.api.infrastructure.exception.custom.ServiceException;
import com.zipsoon.api.infrastructure.exception.model.ErrorCode;
import com.zipsoon.api.interfaces.api.estate.dto.*;
import com.zipsoon.common.domain.Estate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.zipsoon.api.infrastructure.exception.model.ErrorCode.ESTATE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class EstateService {
    private final ScoreService scoreService;
    private final ApiEstateRepository apiEstateRepository;
    private static final int MAX_RESULTS_PER_ZOOM = 1000;

    @Transactional(readOnly = true)
    public List<EstateResponse> findEstatesInViewport(ViewportRequest request) {
        int limit = calculateLimit(request.zoom());
        List<Estate> estates = apiEstateRepository.findAllInViewport(request, limit);

        if (estates.isEmpty()) {
            throw new ServiceException(ErrorCode.ESTATE_NOT_FOUND, "해당 지역에 매물이 없습니다.");
        }

        return estates.stream()
            .map(estate -> {
                ScoreSummary scoreSummary = scoreService.getScoreSummary(estate.getId());
                return EstateResponse.from(estate, scoreSummary);
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public EstateDetailResponse findEstateDetail(Long id) {
        Estate estate = apiEstateRepository.findById(id)
            .orElseThrow(() -> new ServiceException(ESTATE_NOT_FOUND));

        ScoreDetails scoreDetails = scoreService.getScoreDetails(estate.getId());
        return EstateDetailResponse.from(estate, scoreDetails);
    }

    private int calculateLimit(int zoom) {
        if (zoom >= 14) return 500;
        return MAX_RESULTS_PER_ZOOM;
    }
}
