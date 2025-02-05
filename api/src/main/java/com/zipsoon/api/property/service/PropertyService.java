package com.zipsoon.api.property.service;

import com.zipsoon.api.property.dto.PropertyDetailResponse;
import com.zipsoon.api.property.dto.PropertyResponse;
import com.zipsoon.api.property.dto.ViewportRequest;
import com.zipsoon.api.property.mapper.PropertyMapper;
import com.zipsoon.common.exception.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.zipsoon.common.exception.ErrorCode.PROPERTY_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PropertyService {
   private final PropertyMapper propertyMapper;
   private static final int MAX_RESULTS_PER_ZOOM = 1000;

   @Transactional(readOnly = true)
   public List<PropertyResponse> findPropertiesInViewport(ViewportRequest request) {
       int limit = calculateLimit(request.zoom());
       return propertyMapper.findInViewport(request, limit).stream()
           .map(PropertyResponse::from)
           .collect(Collectors.toList());
   }

   @Transactional(readOnly = true)
   public PropertyDetailResponse findPropertyDetail(Long id) {
       return propertyMapper.findById(id)
           .map(PropertyDetailResponse::from)
           .orElseThrow(() -> new ResourceNotFoundException(PROPERTY_NOT_FOUND));
   }

   private int calculateLimit(int zoom) {
       if (zoom <= 8) return 100;
       if (zoom <= 14) return 500;
       return MAX_RESULTS_PER_ZOOM;
   }
}
