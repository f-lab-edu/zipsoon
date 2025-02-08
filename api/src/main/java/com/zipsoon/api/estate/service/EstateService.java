package com.zipsoon.api.estate.service;

import com.zipsoon.api.estate.dto.EstateDetailResponse;
import com.zipsoon.api.estate.dto.EstateResponse;
import com.zipsoon.api.estate.dto.ViewportRequest;
import com.zipsoon.api.estate.mapper.EstateMapper;
import com.zipsoon.common.exception.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.zipsoon.common.exception.ErrorCode.ESTATE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class EstateService {
   private final EstateMapper estateMapper;
   private static final int MAX_RESULTS_PER_ZOOM = 1000;

   @Transactional(readOnly = true)
   public List<EstateResponse> findEstatesInViewport(ViewportRequest request) {
       int limit = calculateLimit(request.zoom());
       return estateMapper.findAllInViewport(request, limit).stream()
           .map(EstateResponse::from)
           .collect(Collectors.toList());
   }

   @Transactional(readOnly = true)
   public EstateDetailResponse findEstateDetail(Long id) {
       return estateMapper.findById(id)
           .map(EstateDetailResponse::from)
           .orElseThrow(() -> new ResourceNotFoundException(ESTATE_NOT_FOUND));
   }

   private int calculateLimit(int zoom) {
       if (zoom <= 8) return 100;
       if (zoom <= 14) return 500;
       return MAX_RESULTS_PER_ZOOM;
   }
}
